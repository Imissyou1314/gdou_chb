package gdou.gdou_chb.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.kymjs.rxvolley.rx.Result;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import gdou.gdou_chb.R;
import gdou.gdou_chb.model.PayModel;
import gdou.gdou_chb.model.bean.Address;
import gdou.gdou_chb.model.bean.Orders;
import gdou.gdou_chb.model.bean.ResultBean;
import gdou.gdou_chb.model.impl.AddressModelImpl;
import gdou.gdou_chb.model.impl.BaseModelImpl;
import gdou.gdou_chb.model.impl.PayModelImpl;
import gdou.gdou_chb.util.GsonUtils;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


public class PayActivity extends AppCompatActivity implements View.OnClickListener {


    @BindView(R.id.pay_shop_number)
    TextView numberTxt;
    @BindView(R.id.pay_shop_totle)
    TextView totleTxt;
    @BindView(R.id.pay_onlie)
    Button payBtn;
    @BindView(R.id.pay_no_onlie)
    Button noPayBtn;

    @BindView(R.id.pay_address_list)
    RecyclerView addressListView;

    private List<Address> mAddressList;
    private CommonAdapter<Address> mAdapter;
    private PayModel mPayModel;                 //支付数据

    private boolean isPayView = false;          //是否跳转到支付页面
    private double cost;
    private int count;

    private Integer mOrdersId;
    private String goodsList;

    private Orders tempOrders;          //临时提交的订单了
    private int selectAddressId;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        ButterKnife.bind(this);
        initData();
        initView();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void initView() {

        numberTxt.setText(numberTxt.getText().toString() + count);
        totleTxt.setText(totleTxt.getText().toString() + cost);

        mAdapter = new CommonAdapter<Address>(this, R.layout.item_address, mAddressList) {
            @Override
            protected void convert(ViewHolder holder, Address address, int position) {
                holder.setText(R.id.address_item_address_phone, address.getPhone());
                holder.setText(R.id.address_item_address_username, address.getName());
                holder.setText(R.id.address_item_address_name, address.getAddressName());
            }
        };

        mAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                selectAddressId = (int) mAddressList.get(position).getId();
                Toast.makeText(getApplication(), "你选择了" + position, Toast.LENGTH_LONG).show();
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });

        addressListView.setAdapter(mAdapter);
        addressListView.setLayoutManager(new LinearLayoutManager(this));

        payBtn.setOnClickListener(this);
        noPayBtn.setOnClickListener(this);
    }

    private void initData() {

        mAddressList = new ArrayList<>();
        mPayModel = new PayModelImpl();
        if (null != getIntent().getStringExtra("orders")) {
            tempOrders = GsonUtils.parseJsonWithGson(getIntent().getStringExtra("orders"), Orders.class);
            cost = getIntent().getDoubleExtra("cost", 0);
            count = getIntent().getIntExtra("count", 0);
            goodsList = getIntent().getStringExtra("goodsList");
        }

        new AddressModelImpl().all(BaseModelImpl.user.getId()).map(new Func1<Result, String>() {
            @Override
            public String call(Result result) {
                return new String(result.data);
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<String>() {
                               @Override
                               public void onCompleted() {
                                   Log.d("PayActivity", "succ");
                               }

                               @Override
                               public void onError(Throwable e) {
                                   Log.d("PayActivity==>", "error");
                                   Log.e("PayActivity", "登录错误信息", e);
                               }

                               @Override
                               public void onNext(String string) {
                                   Log.d("Login", "onNext: " + string);
                                   ResultBean resultBean = GsonUtils.getResultBeanByJson(string);
                                   //进行数据处理

                                   List<Address> addresses = GsonUtils
                                           .getBeanFromResultBeanListMiss(resultBean,
                                                   "addressList", Address[].class);
                                   mAddressList.addAll(addresses);
                                   mAdapter.notifyDataSetChanged();
                               }
                           }
                );
    }

    @Override
    public void onClick(View v) {

        tempOrders.setAddressId(selectAddressId);
        if (null != BaseModelImpl.user)
            tempOrders.setUserId(BaseModelImpl.user.getId());
        if (v.getId() == R.id.pay_no_onlie) {
            //货到付款
            placeOrders(tempOrders, goodsList);
            isPayView = true;
        } else {
            //在线支付
            placeOrders(tempOrders, goodsList);
            isPayView = false;
        }
    }

    /**
     * 提交订单
     */
    private void placeOrders(Orders orders, String goodsList) {
        mPayModel.placeOrder(orders, goodsList).map(new Func1<Result, String>() {

            @Override
            public String call(Result result) {
                return new String(result.data);
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<String>() {
                               @Override
                               public void onCompleted() {
                                   Log.d("PayActivity", "succ");
                                   if (isPayView) {
                                       getPayView(mOrdersId);
                                   }
                               }

                               @Override
                               public void onError(Throwable e) {
                                   Log.d("PayActivity==>", "error");
                                   Log.e("PayActivity", "登录错误信息", e);
                               }

                               @Override
                               public void onNext(String string) {
                                   Log.d("Login", "onNext: " + string);
                                   ResultBean resultBean = GsonUtils.getResultBeanByJson(string);
                                   //进行数据处理
                                   if (resultBean.isServiceResult()) {
                                       mOrdersId = Integer.parseInt(resultBean.getResultParm().get("ordersId").toString());
                                   } else {
                                       isPayView = false;
                                   }
                               }
                           }
                );
    }

    /**
     * 去选择支付页面
     *
     * @param ordersId
     */
    private void getPayView(Integer ordersId) {
        Intent intent = new Intent(this, SelectPayActivity.class);
        intent.putExtra("ordersId", ordersId);
        startActivity(intent);
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Pay Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
