package gdou.gdou_chb.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.kymjs.rxvolley.rx.Result;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import gdou.gdou_chb.R;
import gdou.gdou_chb.model.OrderModel;
import gdou.gdou_chb.model.bean.OrderDetail;
import gdou.gdou_chb.model.bean.Orders;
import gdou.gdou_chb.model.bean.ResultBean;
import gdou.gdou_chb.model.impl.OrderModelImpl;
import gdou.gdou_chb.util.GsonUtils;
import gdou.gdou_chb.util.Java.BaseActivity;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by WT on 2016/12/9.
 */

public class OrderdetailActivity extends BaseActivity {

    @BindView(R.id.order_detail_userName)
    TextView userName;
    @BindView(R.id.order_detail_address)
    TextView userAddress;
    @BindView(R.id.order_detail_phone)
    TextView userPhone;
    @BindView(R.id.order_detail_totle)
    TextView totlePrice;
    @BindView(R.id.order_detail_order_name)
    TextView shopName;
    @BindView(R.id.order_detail_goods)
    RecyclerView goodsListView;

    private Orders orders;
    private OrderModel ordermodel;
    private List<OrderDetail> goodsList;
    private CommonAdapter<OrderDetail> mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        ButterKnife.bind(this);

        initData();

        initView();
    }

    private void initView() {
        goodsListView.setLayoutManager(new LinearLayoutManager(this));

        userName.setText(userName.getText().toString() + orders.getName());
        userAddress.setText(userAddress.getText().toString() + orders.getAddress());
        userPhone.setText(userPhone.getText().toString() + orders.getPhone());
        totlePrice.setText(totlePrice.getText().toString() + orders.getTotalPrice());
        shopName.setText(shopName.getText().toString() + orders.getName());

        mAdapter = new CommonAdapter<OrderDetail>(this, R.layout.item_order_detail, goodsList) {
            @Override
            protected void convert(ViewHolder holder, OrderDetail orderDetail, int position) {
                holder.setText(R.id.item_order_detail_goodname, orderDetail.getGoodName());
                holder.setText(R.id.item_order_detail_price, "单价 :" + orderDetail.getPrice() + "");
                holder.setText(R.id.item_order_detail_totle, "合计 :" + orderDetail.getTotalPrice() + "");
                holder.setText(R.id.item_order_detail_number, "数量 :" + orderDetail.getNumber() + "");
            }
        };
        goodsListView.setAdapter(mAdapter);

    }

    private void initData() {
        goodsList = new ArrayList<>();
        if (null == getIntent().getStringExtra("order"))
            return;
        orders = GsonUtils.parseJsonWithGson(getIntent().getStringExtra("order"), Orders.class);

        initViewData();
        ordermodel = new OrderModelImpl();
        ordermodel.getOrderDetail(Long.valueOf(orders.getId()))
                .map(new Func1<Result, String>() {
                                                    @Override
                                                    public String call(Result result) {
                                                        return new String(result.data);
                                                    }
                                                } )
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribeOn(Schedulers.io())
                                                .subscribe(new Subscriber<String>() {
                                                               @Override
                                                               public void onCompleted() {
                                                                   Log.d("OrderdetailActivity", "succ");
                                                               }

                                                               @Override
                                                               public void onError(Throwable e) {
                                                                   Log.d("OrderdetailActivity","error");
                                                                   Log.e("OrderdetailActivity", "onError: 错误信息",e );
                                                               }

                                                               @Override
                                                               public void onNext(String string) {
                                                                   Log.d("OrderdetailActivity","result:" + string);
                                                                   ResultBean resultBean = GsonUtils.getResultBeanByJson(string);
                                                                   //解析成对应的对象
                                                                   List<OrderDetail> orderDetaList = GsonUtils
                                                                           .getBeanFromResultBeanListMiss(resultBean,"orderDetailList",
                                                                                   OrderDetail[].class);
                                                                   goodsList.addAll(orderDetaList);
                                                                   mAdapter.notifyDataSetChanged();
                                                               }
                                                           }
                                                );

    }

    private void initViewData() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
