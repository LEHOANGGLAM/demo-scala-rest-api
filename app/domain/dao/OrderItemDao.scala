package domain.dao

import com.google.inject.{ImplementedBy, Inject, Singleton}
import domain.models.OrderItem
import domain.tables.{OrderItemTable}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

@ImplementedBy(classOf[OrderItemDaoImpl])
trait OrderItemDao {
  def find(id: Long): Future[Option[OrderItem]]
  def listAll(): Future[Iterable[OrderItem]]
  def save(orderItem: OrderItem): Future[OrderItem]
  def saveAll(list: Seq[OrderItem]): Future[Seq[OrderItem]]
  def update(orderItem: OrderItem): Future[OrderItem]
  def delete(id: Long): Future[Int]
}
@Singleton
class OrderItemDaoImpl @Inject()(daoRunner: DaoRunner)(implicit ec: DbExecutionContext)
  extends OrderItemDao {

  private val orderItems = TableQuery[OrderItemTable]

  override def find(id: Long): Future[Option[OrderItem]] = daoRunner.run {
    orderItems.filter(_.id === id).result.headOption
  }

  override def listAll(): Future[Iterable[OrderItem]] = daoRunner.run {
    orderItems.result
  }

  override def save(orderItem: OrderItem): Future[OrderItem] = daoRunner.run {
    orderItems returning orderItems += orderItem
  }

  override def saveAll(list: Seq[OrderItem]): Future[Seq[OrderItem]] = daoRunner.run {
    (orderItems ++= list).map(_ => list)
  }

  override def update(orderItem: OrderItem): Future[OrderItem] = daoRunner.run {
    orderItems.filter(_.id === orderItem.id).update(orderItem).map(_ => orderItem)
  }

  override def delete(id: Long): Future[Int] = daoRunner.run {
    orderItems.filter(_.id === id).delete
  }
}