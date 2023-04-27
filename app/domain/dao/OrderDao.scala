package domain.dao

import com.google.inject.{ImplementedBy, Inject, Singleton}
import domain.models.Order
import domain.tables.{OrderTable}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

@ImplementedBy(classOf[OrderDaoImpl])
trait OrderDao {
  def find(id: Long): Future[Option[Order]]
  def listAll(): Future[Iterable[Order]]
  def save(order: Order): Future[Order]
  def saveAll(list: Seq[Order]): Future[Seq[Order]]
  def update(order: Order): Future[Order]
  def delete(id: Long): Future[Int]
}
@Singleton
class OrderDaoImpl @Inject()(daoRunner: DaoRunner)(implicit ec: DbExecutionContext)
  extends OrderDao {

  private val orders = TableQuery[OrderTable]

  override def find(id: Long): Future[Option[Order]] = daoRunner.run {
    orders.filter(_.id === id).result.headOption
  }

  override def listAll(): Future[Iterable[Order]] = daoRunner.run {
    orders.result
  }

  override def save(order: Order): Future[Order] = daoRunner.run {
    orders returning orders += order
  }

  override def saveAll(list: Seq[Order]): Future[Seq[Order]] = daoRunner.run {
    (orders ++= list).map(_ => list)
  }

  override def update(order: Order): Future[Order] = daoRunner.run {
    orders.filter(_.id === order.id).update(order).map(_ => order)
  }

  override def delete(id: Long): Future[Int] = daoRunner.run {
    orders.filter(_.id === id).delete
  }
}