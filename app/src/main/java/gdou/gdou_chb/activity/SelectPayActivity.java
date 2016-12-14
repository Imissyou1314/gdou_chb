package gdou.gdou_chb.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.kymjs.rxvolley.rx.Result;
import com.pingplusplus.android.Pingpp;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gdou.gdou_chb.R;
import gdou.gdou_chb.model.bean.ResultBean;
import gdou.gdou_chb.model.impl.PayModelImpl;
import gdou.gdou_chb.util.GsonUtils;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by MissYou on 2016/12/14.
 * 选择支付页面
 */
public class SelectPayActivity extends Activity {

    @BindView(R.id.select_pay_zhifubao)
    ImageView zhifubaoPay;
    @BindView(R.id.select_pay_weixin)
    ImageView weixinPay;

    /**
     * 微信支付渠道
     */
    private static final String CHANNEL_WECHAT = "wx";

    /**
     * 支付支付渠道
     */
    private static final String CHANNEL_ALIPAY = "alipay";


    private Integer mOrdersId;
    private String payWay;      //支付方式

    private String pay_data = "qwalletXXXXXXX" ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_pay_activity);
        ButterKnife.bind(this);

        initData();
    }

    private void initData() {
        mOrdersId = getIntent().getIntExtra("ordersId", 0);
    }


    @OnClick(R.id.select_pay_zhifubao)
    public void zhifubaoPay() {
        payWay = CHANNEL_ALIPAY;
        if (0 != mOrdersId) {
            payOrders(mOrdersId, payWay);
        }
    }

    @OnClick(R.id.select_pay_weixin)
    public void weixinPay() {
        payWay = CHANNEL_WECHAT;
        if (0 != mOrdersId) {
            payOrders(mOrdersId, payWay);
        }
    }

    /**
     * 支付订单
     * @param mOrdersId
     * @param payWay
     */
    private void payOrders(Integer mOrdersId, String payWay) {
        new PayModelImpl().payOrder(mOrdersId, payWay)
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
                                                   Log.d("SelectPayActivity", "succ");
                                               }

                                               @Override
                                               public void onError(Throwable e) {
                                                   Log.d("SelectPayActivity==>","error");
                                                   Log.e("SelectPayActivity", "登录错误信息", e);
                                               }

                                               @Override
                                               public void onNext(String string) {
                                                   Log.d("Login", "onNext: " + string);
                                                   ResultBean resultBean = GsonUtils.getResultBeanByJson(string);
                                                   if (resultBean.isServiceResult()) {
                                                        String charge = GsonUtils.getJsonStr(resultBean.getResultParm().get("charge"));

                                                       toPay(charge);
                                                   }
                                               }
                                           }
                                );
    }

    private void toPay(String charge) {
        Pingpp.createPayment(this, charge);
    }


}
