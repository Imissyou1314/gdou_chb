package gdou.gdou_chb.model;

import com.kymjs.rxvolley.rx.Result;

import gdou.gdou_chb.model.bean.Goods;
import gdou.gdou_chb.model.bean.Orders;
import gdou.gdou_chb.model.bean.Shop;
import gdou.gdou_chb.model.bean.User;
import rx.Observable;

/**
 * Created by WT on 2016/11/30.
 */
public interface OrderModel {
    /**
     * 用户下单
     *
     * @param orders
     * @param goodsList
     * @return
     */
    Observable<Result> placeOrder(Orders orders, String goodsList);

    /**
     * 用户完成订单
     *
     * @param orderId
     * @return
     */
    Observable<Result> doneOrders(Long orderId);

    /**
     * 用户申请退单
     *
     * @param orderId
     * @return
     */
    Observable<Result> rebackOrders(Long orderId);

    /**
     * 查找用户所有订单
     *
     * @param userId
     * @return
     */
    Observable<Result> UserAllOrders(Long userId);

    /**
     * 查看订单详情
     * @param orderId
     * @return
     */
    Observable<Result> getOrderDetail(Long orderId);
}
