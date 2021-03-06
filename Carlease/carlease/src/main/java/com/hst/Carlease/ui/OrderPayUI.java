package com.hst.Carlease.ui;

import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.hst.Carlease.R;
import com.hst.Carlease.asynchttp.AsyncCallBackHandler;
import com.hst.Carlease.asynchttp.AsyncHttpUtil;
import com.hst.Carlease.constants.Constants;
import com.hst.Carlease.eventBean.Stype;
import com.hst.Carlease.http.bean.AlipaypayBean2;
import com.hst.Carlease.http.bean.Bean;
import com.hst.Carlease.http.bean.RegisterBean;
import com.hst.Carlease.pay.Alipay2;
import com.hst.Carlease.pay.Alipay2.OnAlipayListener;
import com.hst.Carlease.ram.Constant;
import com.hst.Carlease.ram.Http_Url;
import com.hst.Carlease.util.SPUtils;
import com.hst.Carlease.util.StringUtils;
import com.hst.Carlease.widget.mywidget.ToastL;
import com.tools.app.AbsUI;
import com.tools.app.TitleBar;
import com.tools.json.GJson;
import com.tools.net.NetworkState;
import com.tools.util.Log;
import com.tools.widget.Prompt;
import com.weixin.paydemo.WXPay;

public class OrderPayUI extends AbsUI {
	private final String TAG = PayUI.class.getSimpleName();
	private TitleBar titleBar;
	private TextView Pay, feetype, tv_hetong;// 支付金额
	private CheckBox alipay, weixin;

	private RelativeLayout RV_feetype, RV_HETONG;
	private EditText money;
	private Intent intent;
	private String orderNO;
	private int dataFrom;
	private String body;
	private int contactid;
	private String price;
	private String totalmoney = "";
	private String subject = "";// 描述

	@Override
	protected void onCreate(Bundle arg0) {
		setContentView(R.layout.ui_pay);
		EventBus.getDefault().register(this);
		setSlideFinishEnabled(false);
		super.onCreate(arg0);
	}

	@Override
	protected void initControl() {
		titleBar = new TitleBar();
		Pay = (TextView) findViewById(R.id.btn_next);
		money = (EditText) findViewById(R.id.money);
		RV_feetype = (RelativeLayout) findViewById(R.id.RV_feetype);
		RV_HETONG = (RelativeLayout) findViewById(R.id.RV_HETONG);
		feetype = (TextView) findViewById(R.id.feetype);
		tv_hetong = (TextView) findViewById(R.id.tv_hetong);
		alipay = (CheckBox) findViewById(R.id.alipay);
		weixin = (CheckBox) findViewById(R.id.weixin);
		RV_feetype.setVisibility(View.GONE);
		RV_HETONG.setVisibility(View.GONE);
		money.setGravity(Gravity.RIGHT);
		intent = getIntent();
		orderNO = intent.getStringExtra("orderNo");
		dataFrom = intent.getIntExtra("dataFrom", 1);
		body = intent.getStringExtra("body");
		contactid = intent.getIntExtra("contactid", 0);
		if (dataFrom == Constant.ORDER_PAY4) {
			totalmoney = intent.getStringExtra("money");
			money.setText(totalmoney);
			money.setFocusable(false);
			subject = intent.getStringExtra("subject");
		}else if(dataFrom == Constant.ORDER_PAY3){
			totalmoney = intent.getStringExtra("money");
			money.setText(totalmoney);
		}
		Log.i("mypayAdapter", "单号==Adapter===" + orderNO);
		Log.i("mypayAdapter", "body==Adapter===" + body);
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "onResume()");
		super.onResume();
	}

	@Override
	protected void initControlEvent() {
		money.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!money.getText().toString().isEmpty()) {
					money.setSelection(money.getText().toString().length());
				}
			}
		});
		Pay.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				price = money.getText().toString();
				if (isEmptyString(price)) {
					ToastL.show("请输入付款金额");
					return;
				}
				if (!StringUtils.isPrice(price)) {
					ToastL.show("金额输入有误，请重新输入！");
					return;
				}
				float money = Float.parseFloat(price);
				Log.i(TAG, "price===" + price);
				Log.i(TAG, "money===" + money);
				if (money == 0) {
					ToastL.show("请输入大于0的金额");
					return;
				}
				if (money > 100000) {
					ToastL.show("请输入小于100000的金额");
					return;
				}
				if (alipay.isChecked()) {
					// GoAlipay();
					GoAlipayApp();
				} else if (weixin.isChecked()) {
					// 调用微信支付
					try {
						if (dataFrom == 4) {
							WXPay.getInstance(OrderPayUI.this).getPayParamers(
									contactid, price, body, ui, Pay, subject,
									orderNO, dataFrom + "");
						} else {
							WXPay.getInstance(OrderPayUI.this).getPayParamers(
									contactid, price, body, ui, Pay,
									"深圳市赢时通汽车服务有限公司", orderNO, dataFrom + "");
						}

					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				} else {
					ToastL.show("请选择付款方式");
				}

			}
		});
		alipay.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					weixin.setChecked(false);
				}
			}
		});
		weixin.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					alipay.setChecked(false);
				}
			}
		});
	}

	// 支付宝支付方法2
	private void GoAlipayApp() {
		NetworkState nState = new NetworkState(ui);
		if (nState.isConnected() == false) {
			Prompt.showWarning(ui, "无网络连接");
			return;
		}
		AlipaypayBean2 bean2 = new AlipaypayBean2();
		bean2.setBody(body);
		bean2.setContract(contactid);
		bean2.setExtend_params("");
		bean2.setGoods_type("1");
		bean2.setPromo_params("");
		if (dataFrom == 4) {
			bean2.setSubject("深圳市赢时通汽车服务有限公司违章代办");
		} else {
			bean2.setSubject("深圳市赢时通汽车服务有限公司订单支付");
		}
		bean2.setToken(SPUtils.get(ui, Constants.tokenID, "").toString());
		bean2.setTotal_fee(price);
		bean2.setDataFrom(dataFrom + "");
		bean2.setOrderNo(orderNO);
		try {
			AsyncHttpUtil.post(ui, Http_Url.AliPayAppImpl, bean2,
					"application/json", new AsyncCallBackHandler(ui, "", true,
							Pay) {
						@Override
						public void myFailure(int arg0, Header[] arg1,
								String arg2, Throwable arg3) {
							Log.e(TAG, "Http---error==" + arg0 + "---" + arg3);
						}

						@Override
						public void mySuccess(int arg0, Header[] arg1,
								String arg2) {
							Log.i(TAG, "result===" + arg2);
							if (arg2 == null) {
								return;
							}
							Bean bean = GJson.parseObject(arg2, Bean.class);
							if (bean != null) {
								RegisterBean PayBean2 = GJson.parseObject(
										bean.getD(), RegisterBean.class);
								if (PayBean2 != null) {
									if (PayBean2.getStatu() == 1) {
										if (PayBean2.getModel() == null) {
											return;
										}
										Alipay2 pay = new Alipay2(ui, ui,
												PayBean2.getModel());
										pay.initPayType();
										pay.setListener(new OnAlipayListener() {
											@Override
											public void onSuccess() {
												// 当数据为空时的显示问题
												ToastL.show("支付成功");
												if (dataFrom == Constant.ORDER_PAY2) {
													EventBus.getDefault()
															.post(new Stype(
																	dataFrom));
													finish();
												} else if (dataFrom == Constant.ORDER_PAY3) {
													EventBus.getDefault()
															.post(new Stype(
																	dataFrom));
													finish();
												} else {
													EventBus.getDefault()
															.post(new Stype(
																	dataFrom));
													finish();
												}
												// 当订单取消完了
												super.onSuccess();
											}

											@Override
											public void onCancel() {
												ToastL.show("支付已取消");
												// 通知我界面更新数据
												super.onCancel();
											}

											@Override
											public void onWait() {
												ToastL.show("正在支付...请稍后");
												super.onWait();
											}
										});
									} else if (PayBean2.getStatu() == -2) {
										ToastL.show(PayBean2.getMsg());
										StringUtils.IsOUTOFtime(context,
												OrderPayUI.this.ui);
									} else {
										ToastL.show("获取密钥失败");
									}

								}

							}

						}

					});

		} catch (UnsupportedEncodingException e) {

			e.printStackTrace();
		}
	}

	@Override
	public void onAttachedToWindow() {
		titleBar.setTitle("移动支付");
		titleBar.getLeftView(1).setVisibility(View.VISIBLE);
		titleBar.getLeftView(1).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		super.onAttachedToWindow();
	}

	@Override
	protected void onStartLoader() {

	}

	@Override
	protected byte[] doInBackgroundLoader() {
		return null;
	}

	@Override
	protected void onFinishedLoader(Loader<byte[]> loader, byte[] bytes) {

	}

	@Override
	protected void initMember() {
		addFgm(R.id.titlebar, titleBar);
	}

	@Override
	protected void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	@Subscribe
	public void onEventMainThread(Stype ev) {
		if (ev.getMsg() ==Constant.wx_pay) {
			EventBus.getDefault().post(new Stype(dataFrom));
			finish();
		}
	}

}
