package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import domain.dao.{OrderItemDao}
import domain.models.{OrderItem}

import scala.concurrent.{ExecutionContext, Future}


@ImplementedBy(classOf[OrderItemServiceImpl])
trait OrderItemService {
  def findByOrderId(id: Long): Future[Seq[OrderItem]]

}


@Singleton
class OrderItemServiceImpl @Inject()(orderItemDao: OrderItemDao)(implicit ex: ExecutionContext) extends OrderItemService {
  override def findByOrderId(id: Long): Future[Seq[OrderItem]] =
    orderItemDao.findByOrderId(id)
}

