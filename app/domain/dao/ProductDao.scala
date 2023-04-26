package domain.dao

import com.google.inject.{ImplementedBy, Inject, Singleton}
import domain.models.Product
import domain.tables.{ProductTable, UserTable}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

@ImplementedBy(classOf[ProductDaoImpl])
trait ProductDao {
  def find(id: Long): Future[Option[Product]]
  def listAll(): Future[Iterable[Product]]
  def save(user: Product): Future[Product]
  def saveAll(list: Seq[Product]): Future[Seq[Product]]
  def update(product: Product): Future[Product]
  def delete(id: Long): Future[Int]
}
@Singleton
class ProductDaoImpl @Inject()(daoRunner: DaoRunner)(implicit ec: DbExecutionContext)
  extends ProductDao {

  private val products = TableQuery[ProductTable]

  override def find(id: Long): Future[Option[Product]] = daoRunner.run {
    products.filter(_.id === id).result.headOption
  }

  override def listAll(): Future[Iterable[Product]] = daoRunner.run {
    products.result
  }

  override def save(product: Product): Future[Product] = daoRunner.run {
    products returning products += product
  }

  override def saveAll(list: Seq[Product]): Future[Seq[Product]] = daoRunner.run {
    (products ++= list).map(_ => list)
  }

  override def update(product: Product): Future[Product] = daoRunner.run {
    products.filter(_.id === product.id).update(product).map(_ => product)
  }

  override def delete(id: Long): Future[Int] = daoRunner.run {
    products.filter(_.id === id).delete
  }
}