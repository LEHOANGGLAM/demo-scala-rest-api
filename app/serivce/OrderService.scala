package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import domain.dao.{OrderDao, OrderItemDao}
import domain.models.{Order, OrderItem}

import scala.concurrent.{Await, ExecutionContext, Future}


@ImplementedBy(classOf[OrderServiceImpl])
trait OrderService {


  def find(id: Long): Future[Option[Order]]


  def listAll(): Future[Iterable[Order]]


  def save(order: Order, orderItems: Seq[OrderItem]): Future[Order]


  def update(order: Order, orderItems: Seq[OrderItem]): Future[Order]


  def delete(id: Long): Future[Int]
}


@Singleton
class OrderServiceImpl @Inject()(orderDao: OrderDao, orderItemDao: OrderItemDao)(implicit ex: ExecutionContext) extends OrderService {
  override def find(id: Long): Future[Option[Order]] = {
    orderDao.find(id).map {
      case Some(order) => Some(order)
      case None => None
    }
  }

  override def listAll(): Future[Iterable[Order]] = {
    orderDao
      .listAll()
  }

  override def save(
                     order: Order, orderItems: Seq[OrderItem]
                   ): Future[Order] = {
    for {
      savedOrder <- orderDao.save(order)
      savedOrderItems = orderItems.map(item => item.copy(orderId = savedOrder.id.get))
      _ <- orderItemDao.saveAll(savedOrderItems)
    } yield savedOrder
  }

    override def update(order: Order, newOrderItems: Seq[OrderItem]): Future[Order] ={
      for {
        savedOrder <- orderDao.update(order)
        updatedOrderItems <- orderItemDao.updateAll(newOrderItems)
      } yield savedOrder
    }

    override def delete(id: Long): Future[Int] = orderDao.delete(id)

}

