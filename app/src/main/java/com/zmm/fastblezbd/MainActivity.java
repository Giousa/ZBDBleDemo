package com.zmm.fastblezbd;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleIndicateCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.zmm.fastblezbd.adapter.BleListAdapter;
import com.zmm.fastblezbd.utils.CheckUtils;
import com.zmm.fastblezbd.utils.CrcUtil2;
import com.zmm.fastblezbd.utils.FileUtils;
import com.zmm.fastblezbd.utils.StringUtils;
import com.zmm.fastblezbd.utils.ToastUtils;
import com.zmm.fastblezbd.utils.TypeUtil;
import com.zmm.fastblezbd.view.LovelyProgressBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements BleListAdapter.OnDeviceClickListener {

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    //点击按钮
    @BindView(R.id.scan_ble)
    Button mScanBle;
    @BindView(R.id.btn_start)
    Button mBtnStart;
    @BindView(R.id.btn_pause)
    Button mBtnPause;
    @BindView(R.id.btn_stop)
    Button mBtnStop;
    @BindView(R.id.btn_clear)
    Button mBtnClear;
    @BindView(R.id.btn_device_set)
    Button mBtnDeviceSet;


    //日志
    @BindView(R.id.tv_content)
    TextView mTvContent;

    //返回参数
    @BindView(R.id.tv_model)
    TextView mTvModel;
    @BindView(R.id.tv_speed_level)
    TextView mTvSpeedLevel;
    @BindView(R.id.tv_speed_value)
    TextView mTvSpeedValue;
    @BindView(R.id.tv_speed_offset)
    TextView mTvSpeedOffset;
    @BindView(R.id.tv_spasm_num)
    TextView mTvSpasmNum;
    @BindView(R.id.tv_spasm_level)
    TextView mTvSpasmLevel;
    @BindView(R.id.tv_resistance)
    TextView mTvResistance;
    @BindView(R.id.tv_intelligence)
    TextView mTvIntelligence;
    @BindView(R.id.tv_direction)
    TextView mTvDirection;


    //设置参数
    @BindView(R.id.rb_model_bei)
    RadioButton mRbModelBei;
    @BindView(R.id.rb_model_zhu)
    RadioButton mRbModelZhu;
    @BindView(R.id.rb_intel_open)
    RadioButton mRbIntelOpen;
    @BindView(R.id.rb_intel_close)
    RadioButton mRbIntelClose;
    @BindView(R.id.rb_direc_zheng)
    RadioButton mRbDirecZheng;
    @BindView(R.id.rb_direc_fan)
    RadioButton mRbDirecFan;
    @BindView(R.id.et_time)
    EditText mEtTime;
    @BindView(R.id.et_speed)
    EditText mEtSpeed;
    @BindView(R.id.et_spasm)
    EditText mEtSpasm;
    @BindView(R.id.et_resistance)
    EditText mEtResistance;
    @BindView(R.id.loadbar)
    LovelyProgressBar mLoadbar;


    private BleListAdapter mBleListAdapter;
    private ProgressDialog progressDialog;


    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;

    private BleDevice mBleDevice;


    private int mRes;
    private int mSpasmLevel;
    private int mSpasmNum;
    private int mOffset;
    private int mSpeedValue;
    private int mSpeedLevel;
    private byte mModel;
    private byte mIntelligence;
    private byte mDirection;

    private boolean isOk = false;


    private String filePath = "/mnt/sdcard/fls/RKF-H1_B02-2018.04.08.fls";

    private long threadTime = 100;

    private boolean isSendStart = false;
    private boolean isChanpinZhen = false;
    private boolean isBanbenZhen = false;
    private boolean isSendDizhiZhen = false;
    private boolean isSendShujubao = false;

    private int index = 0;//当前索引
    private byte[] mBytes;
    private int mCount;//总个数
    private int mLength;//实际长度

    private CountDownTimer mCountDownTimer;
    private long mCurrentTimeMillis;
    private boolean isFirst = true;
    int progress = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mLoadbar.startload();

        initRecyclerView();
        initBLE();

        mCurrentTimeMillis = System.currentTimeMillis();

        mCountDownTimer = new CountDownTimer(10000000, 100) {
            @Override
            public void onTick(long l) {

                if (isSendStart) {
                    long l1 = System.currentTimeMillis() - mCurrentTimeMillis;

//                    System.out.println("每次间隔时长：l1 = "+l1);

                    if(isFirst){
                        progress = index*100/mCount;
                    }else {
                        int i = index*100/ mCount;
                        if(i > progress){
                            progress = i;
                        }
                    }

//                    System.out.println("当前进度：progress = "+progress);
                    mLoadbar.setProgress(progress);

                    if (l1 > 2000) {

                        mCountDownTimer.cancel();

                        isFirst = false;

                        System.out.println("超时1000ms，继续发送地址帧");
                        int i = index * 256 / 4096;
                        index = i * 4096 / 256;
                        isSendDizhiZhen = true;
                        isSendShujubao = false;
                        //超时，继续发送地址帧
                        sendQishizhen();
                    }
                }


            }

            @Override
            public void onFinish() {
            }
        };



    }

    private void initRecyclerView() {

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mBleListAdapter = new BleListAdapter(this);

        mRecyclerView.setAdapter(mBleListAdapter);
        mBleListAdapter.setOnDeviceClickListener(this);

        progressDialog = new ProgressDialog(this);
    }

    private void initBLE() {
        BleManager.getInstance().
                init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setMaxConnectCount(7)
                .setOperateTimeout(30000);

        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setScanTimeOut(0)              // 扫描超时时间，可选，默认10秒；小于等于0表示不限制扫描时间
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);


    }


    @OnClick({R.id.scan_ble, R.id.btn_start, R.id.btn_pause, R.id.btn_stop, R.id.btn_clear, R.id.btn_qishi, R.id.btn_chanpin, R.id.btn_banben, R.id.btn_dizhi, R.id.btn_shuju, R.id.btn_fuwei, R.id.btn_device_set})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.scan_ble:
                mRecyclerView.setVisibility(View.VISIBLE);
                if (mScanBle.getText().equals(getString(R.string.start_scan))) {
                    //手机测试
                    checkPermissions();
//                    机顶盒测试，不开权限
//                    startScan();
                } else if (mScanBle.getText().equals(getString(R.string.stop_scan))) {
                    BleManager.getInstance().cancelScan();
                }
                break;
            case R.id.btn_start:
                if (isOk) {
                    writeBleData(CommonConfig.startByte);
                } else {
                    ToastUtils.SimpleToast("请连接设备");
                }
                break;
            case R.id.btn_pause:
                if (isOk) {
                    writeBleData(CommonConfig.pauseByte);
                } else {
                    ToastUtils.SimpleToast("请连接设备");
                }
                break;
            case R.id.btn_stop:
                if (isOk) {
                    writeBleData(CommonConfig.stopByte);
                } else {
                    ToastUtils.SimpleToast("请连接设备");
                }
                break;
            case R.id.btn_clear:
                mTvContent.setText("");
                break;
            case R.id.btn_qishi:
                sendQishizhen();
                break;
            case R.id.btn_chanpin:
                sendChanpinzhen();
                break;
            case R.id.btn_banben:
                sendBanbenzhen();
                break;
            case R.id.btn_dizhi:
                sendDizhizhen();
                break;
            case R.id.btn_shuju:
                sendShujuzhen();
                break;
            case R.id.btn_fuwei:
                sendFuweizhen();
                break;
            case R.id.btn_device_set:
                if (isOk) {
                    setParam();
//                    writeBleData(CommonConfig.setByte);
                } else {
                    ToastUtils.SimpleToast("请连接设备");
                }
                break;
        }
    }


    /**
     * 处理数据
     */
    private void dealData() {
        if (mModel == 0x01) {
            mTvModel.setText("模式:被动");
        } else {
            mTvModel.setText("模式:主动");
        }

        mTvSpeedLevel.setText("速度档位：" + mSpeedLevel);
        mTvSpeedValue.setText("转速：" + mSpeedValue);
        mTvSpeedOffset.setText("偏移：" + mOffset);
        mTvSpasmLevel.setText("痉挛等级：" + mSpasmLevel);
        mTvSpasmNum.setText("痉挛次数：" + mSpasmNum);
        mTvResistance.setText("阻力：" + mRes);

        if (mIntelligence == 0x40) {
            mTvIntelligence.setText("智能：关闭");
        } else {
            mTvIntelligence.setText("智能：开启");
        }

        if (mDirection == 0x50) {
            mTvDirection.setText("方向：反转");
        } else {
            mTvDirection.setText("方向：正向");
        }

    }


    private void writeBleData(byte[] data) {
        BleManager.getInstance().write(
                mBleDevice,
                "0000ffe1-0000-1000-8000-00805f9b34fb",
                "0000ffe3-0000-1000-8000-00805f9b34fb",
                data,
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        // 发送数据到设备成功
//                        mTvContent.append("writeBleData:发送数据到设备成功\n");
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        // 发送数据到设备失败
//                        mTvContent.append("writeBleData:发送数据到设备失败\n");

                    }
                });
    }


    private void checkPermissions() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, getString(R.string.please_open_blue), Toast.LENGTH_LONG).show();
            return;
        }

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION);
        }
    }

    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.notifyTitle)
                            .setMessage(R.string.gpsNotifyMsg)
                            .setNegativeButton(R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                            .setPositiveButton(R.string.setting,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
                                        }
                                    })

                            .setCancelable(false)
                            .show();

//                    setScanRule();
//                    startScan();
                } else {
                    setScanRule();
                    startScan();
                }
                break;
        }
    }

    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_GPS) {
            if (checkGPSIsOpen()) {
                setScanRule();
                startScan();
            }
        }
    }


    //扫描规则
    private void setScanRule() {
//        name: SOPLAR_HRP_0075  mac: 8C:DE:52:0E:40:D3


    }

    //开始扫描
    private void startScan() {

        System.out.println("开始搜索蓝牙设备......");


        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                mBleListAdapter.clearScanDevice();
                mBleListAdapter.notifyDataSetChanged();
                mScanBle.setText(getString(R.string.stop_scan));
            }

            @Override
            public void onScanning(final BleDevice bleDevice) {

                if (!TextUtils.isEmpty(bleDevice.getDevice().getName())) {
                    System.out.println("onScanning bleDevice::" + bleDevice.getDevice().getName());

                    UIUtils.runInMainThread(new Runnable() {
                        @Override
                        public void run() {
                            mTvContent.append(bleDevice.getDevice().getName() + "\n");
                            mBleListAdapter.addDevice(bleDevice);
                            mBleListAdapter.notifyDataSetChanged();
                        }
                    });

                }

            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                mScanBle.setText(getString(R.string.start_scan));
            }
        });
    }


    @Override
    public void onConnect(BleDevice bleDevice) {
        if (!BleManager.getInstance().isConnected(bleDevice)) {
            BleManager.getInstance().cancelScan();
            connect(bleDevice);
        }
    }

    private void connect(BleDevice bleDevice) {

        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                progressDialog.show();
                mTvContent.append("正在连接...\n");
            }

            @Override
            public void onConnectFail(BleException exception) {
                progressDialog.dismiss();
//                toast("连接失败");
                mTvContent.append("连接失败\n");
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {

                mBleDevice = bleDevice;
                progressDialog.dismiss();
                mBleListAdapter.addDevice(bleDevice);
                mBleListAdapter.notifyDataSetChanged();

//                toast("连接成功");
                ToastUtils.SimpleToast("连接成功");
                mTvContent.append("连接成功\n");
                indicateBle();
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                progressDialog.dismiss();

                mBleListAdapter.removeDevice(bleDevice);
                mBleListAdapter.notifyDataSetChanged();

                if (isActiveDisConnected) {
                    ToastUtils.SimpleToast("断开了");
                    mTvContent.append("断开了\n");
                } else {
                    ToastUtils.SimpleToast("连接断开");
                    mTvContent.append("连接断开\n");

                }


            }
        });
    }


    @Override
    public void onDisConnect(BleDevice bleDevice) {

    }

    @Override
    public void onDetail(BleDevice bleDevice) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BleManager.getInstance().disconnectAllDevice();
        BleManager.getInstance().destroy();
    }


    private byte model;
    private byte time;
    private byte speed;
    private byte spasm;
    private byte resistance;
    private byte intelligence;
    private byte direction;

    /**
     * 设置参数
     */
    private void setParam() {

        StringBuffer sb = new StringBuffer();

        sb.append("设置参数：");

        if (mRbModelBei.isChecked()) {
            model = 0x01;
            sb.append("模式：被动，");
        } else {
            model = 0x02;
            sb.append("模式：主动，");
        }


        Editable mEtTimeText = mEtTime.getText();
        if (mEtTimeText == null) {
            time = 0x05;
            sb.append("时间：5分，");
        } else {
            int timeInt = Integer.valueOf(mEtTimeText.toString().trim());
            time = TypeUtil.int2Byte(timeInt);
            System.out.println("时间参数:" + timeInt);
            sb.append("时间：" + timeInt + ",");
        }

        Editable mEtSpeedText = mEtSpeed.getText();
        if (mEtSpeedText == null) {
            speed = 0x01;
            sb.append("速度：1档，");
        } else {
            int speedInt = Integer.valueOf(mEtSpeedText.toString().trim());
            speed = TypeUtil.int2Byte(speedInt);
            System.out.println("速度参数:" + speedInt);
            sb.append("速度：" + speedInt + "档，");
        }

        Editable mEtSpasmText = mEtSpasm.getText();
        if (mEtSpasmText == null) {
            spasm = 0x01;
            sb.append("痉挛等级：1档，");
        } else {
            int spasmInt = Integer.valueOf(mEtSpasmText.toString().trim());
            spasm = TypeUtil.int2Byte(spasmInt);
            System.out.println("痉挛参数:" + spasmInt);
            sb.append("痉挛等级：" + spasmInt + "档，");
        }


        Editable mEtResistanceText = mEtResistance.getText();
        if (mEtResistanceText == null) {
            resistance = 0x01;
            sb.append("阻力：1档，");
        } else {
            int resistanceInt = Integer.valueOf(mEtResistanceText.toString().trim());
            resistance = TypeUtil.int2Byte(resistanceInt);
            System.out.println("阻力参数:" + resistanceInt);
            sb.append("阻力：" + resistanceInt + "档，");
        }

        if (mRbIntelOpen.isChecked()) {
            intelligence = 0x41;
            sb.append("智能模式：开启，");
        } else {
            intelligence = 0x40;
            sb.append("智能模式：关闭，");
        }

        if (mRbDirecZheng.isChecked()) {
            direction = 0x51;
            sb.append("方向：正转。");
        } else {
            direction = 0x50;
            sb.append("方向：反转。");
        }

        byte[] setByte = {(byte) 0xA3, (byte) 0x20, (byte) 0x21, (byte) 0x81, model, time, speed, spasm, resistance, intelligence, direction};

//        System.out.println("设置参数:"+ Arrays.toString(setByte));
        mTvContent.append(sb.toString() + "\n");
        mTvContent.append("设置参数：" + Arrays.toString(setByte) + "\n");
        writeBleData(setByte);
    }


    /**
     * 起始帧
     */
    private void sendQishizhen() {

        mCountDownTimer.start();
        mCurrentTimeMillis = System.currentTimeMillis();

        index = 0;
        mBytes = null;
        mBytes = FileUtils.File2byte(filePath);
        mLength = mBytes.length;
        System.out.println("mLength = " + mLength);//27726

        if (mLength % 256 == 0) {
            mCount = mLength / 256;
        } else {
            mCount = mLength / 256 + 1;
        }

        try {
            byte[] qishi = {'W', 'T', '3', '9', 'S', 'P', '#', 'S', 'l', 'o', 'w', 0x5D};
            //[61, 120, 62, 60, 111, 107]
//            byte[] b = {'=','x','>','<','o','k'};
//            System.out.println("判断条件："+Arrays.toString(b));

            writeBleData(qishi);

            Thread.sleep(threadTime);

            sendChanpinzhen();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 产品帧
     */
    private void sendChanpinzhen() {

        byte[] bytes = FileUtils.File2byte(filePath);
        System.out.println("bytes  = " + Arrays.toString(bytes));

        byte[] chanpin2 = {'S', 'P', '?', bytes[16], bytes[17], bytes[18], bytes[19]};

        int bcc2 = CheckUtils.getBCC2(chanpin2);

        byte[] chanpin = {'W', 'T', '3', '9', 'S', 'P', '?', bytes[16], bytes[17], bytes[18], bytes[19], (byte) (bcc2 ^ 0x5A)};


        isBanbenZhen = false;
        isChanpinZhen = true;

        mCurrentTimeMillis = System.currentTimeMillis();

        writeBleData(chanpin);

    }

    /**
     * 版本帧
     */
    private void sendBanbenzhen() {

        byte[] bytes = FileUtils.File2byte(filePath);
        System.out.println("bytes  = " + Arrays.toString(bytes));
        byte[] banben2 = {'S', 'P', '^', bytes[12], bytes[13], bytes[14], bytes[15]};
        int bcc2 = CheckUtils.getBCC2(banben2);

        byte[] banben = {'W', 'T', '3', '9', 'S', 'P', '^', bytes[12], bytes[13], bytes[14], bytes[15], (byte) (bcc2 ^ 0x5A)};

        isChanpinZhen = false;
        isBanbenZhen = true;
        writeBleData(banben);
    }

    /**
     * 地址帧
     */
    private void sendDizhizhen() {

        isBanbenZhen = false;
        isChanpinZhen = false;
        isSendShujubao = false;


        isSendStart = true;
        isSendDizhiZhen = true;

        sendIndexDizhizhen();

    }

    private void sendIndexDizhizhen() {

        if (!isSendStart && !isSendDizhiZhen) {
            return;
        }

        //地址帧
        byte[] bytes1 = StringUtils.intToMinByteArray(256 * index + 1933312);
        byte[] dizhi = {'S', 'P', '<', bytes1[0], bytes1[1], bytes1[2], 0x01};
        int bcc2 = CheckUtils.getBCC2(dizhi);
        byte[] dizhi2 = {'W', 'T', '3', '9', 'S', 'P', '<', bytes1[0], bytes1[1], bytes1[2], 0x01, (byte) (bcc2 ^ 0x5A)};
        System.out.println("sendIndexDizhizhen index = " + index);
//        System.out.println("地址帧: "+Arrays.toString(dizhi2));

        //记录时间戳
        mCurrentTimeMillis = System.currentTimeMillis();

        writeBleData(dizhi2);

    }

    private void sendIndexShujubao() {


        if (!isSendStart && !isSendShujubao) {
            return;
        }

        //256字节数组的数据包
        byte[] bytes2 = new byte[256];

//        System.out.println("总个数："+mCount);
//        System.out.println("索引开始："+index*256);
        if (index < mCount - 1) {
            //256个字节

            for (int i = 0; i < 256; i++) {
                bytes2[i] = mBytes[i + index * 256];
            }
        } else if (index == mCount - 1) {
            //不够，补0
            int i = (mCount - 1) * 256;//i = 24576  24832  length = 24648

            for (int j = i; j < 256 + i; j++) {
                if (j >= mLength) {
                    bytes2[j - i] = 0;
                } else {
                    bytes2[j - i] = mBytes[j];
                }
            }

        }

        //CRC校验码 低字节在前,高字节在后
        byte[] bytesCRC = CrcUtil2.setParamCRC(bytes2);

        System.out.println("sendIndexShujubao index = " + index);
        if (index == mCount - 1) {
            System.out.println("最后一条数据包：");
            System.out.println("数据包: " + Arrays.toString(bytesCRC));
        }


        //记录时间戳
        mCurrentTimeMillis = System.currentTimeMillis();

        writeBleData(bytesCRC);
    }

    private void sendShujuzhen() {


        byte[] bytes1 = StringUtils.intToMinByteArray(1933312);

        //00 80 1D 01
        byte[] bytesDi = {0x00, (byte) 0x80, 0x1D, 0x01};
        System.out.println("bytesDi = " + Arrays.toString(bytesDi));
        System.out.println("地址帧：" + Arrays.toString(bytes1));

        byte i = '*';
        System.out.println("i = " + i);
        byte[] bytes = {(byte) 0x9B, (byte) 0xFE, 0x4B, (byte) 0xC4, (byte) 0x9E, (byte) 0x99, 0x0D, 0x48, (byte) 0x90, 0x16};
        System.out.println("最后数据校验：" + Arrays.toString(bytes));


    }

    private void sendFuweizhen() {

        //WT39SP*Boot

        byte[] fuwei = {'S', 'P', '*', 'B', 'o', 'o', 't'};
        int bcc2 = CheckUtils.getBCC2(fuwei);

        byte[] fuwei2 = {'W', 'T', '3', '9', 'S', 'P', '*', 'B', 'o', 'o', 't', (byte) (bcc2 ^ 0x5A)};

        writeBleData(fuwei2);

    }


    /**
     * 打开订阅
     */
    private void indicateBle() {
        BleManager.getInstance().indicate(
                mBleDevice,
                "0000ffe1-0000-1000-8000-00805f9b34fb",
                "0000ffe2-0000-1000-8000-00805f9b34fb",
                new BleIndicateCallback() {


                    @Override
                    public void onIndicateSuccess() {
                        // 打开通知操作成功
//                        toast("通知成功");
                        ToastUtils.SimpleToast("读写成功");
                        mTvContent.append("读写成功\n");
                        isOk = true;
                        //
                        mRecyclerView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onIndicateFailure(BleException exception) {
                        // 打开通知操作失败
//                        toast("通知失败");
                        ToastUtils.SimpleToast("读写失败");
                        mTvContent.append("读写失败\n");
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        //[61, 120, 62, 60, 111, 107]
//                        byte[] b = {'=','x','>','<','o','k'};
                        int length = data.length;
                        String s = Arrays.toString(data);
                        System.out.println("读取数据 = " + s);
                        // 打开通知后，设备发过来的数据将在这里出现

//                        int length = data.length;
//                        String s = Arrays.toString(data);

//                        mTvContent.append("数据个数222："+length+"\n");
//                        mTvContent.append("data222：" + s + "\n");
//                        System.out.println("读取数据 = "+s);
//
                        if (length >= 4) {

                            if (length == 9) {


                                if (data[3] == -128) {

                                    mTvContent.append("数据个数：" + length + "\n");
                                    mTvContent.append("data：" + s + "\n");

                                    mModel = data[4];
                                    mSpeedLevel = data[5] & 0xFF;
                                    mSpeedValue = data[6] & 0xFF;
                                    mOffset = data[7] & 0xFF;
                                    mSpasmNum = data[8] & 0xFF;
                                } else if (data[7] == -122) {

                                    mTvContent.setText("");

                                    mTvContent.append("数据个数：" + length + "\n");
                                    mTvContent.append("停止data：" + s + "\n");
                                    mTvContent.append("————停止————\n");

                                    ToastUtils.SimpleToast("————停止————");
                                }

                            } else if (length == 4) {

                                mTvContent.append("数据个数：" + length + "\n");
                                mTvContent.append("data：" + s + "\n");

                                mSpasmLevel = data[0] & 0xFF;
                                mRes = data[1] & 0xFF;
                                mIntelligence = data[2];
                                mDirection = data[3];
                            } else if (length == 13 && data[3] == -128) {

                                mTvContent.append("数据个数：" + length + "\n");
                                mTvContent.append("data：" + s + "\n");

                                mModel = data[4];
                                mSpeedLevel = data[5] & 0xFF;
                                mSpeedValue = data[6] & 0xFF;
                                mOffset = data[7] & 0xFF;
                                mSpasmNum = data[8] & 0xFF;
                                mSpasmLevel = data[9] & 0xFF;
                                mRes = data[10] & 0xFF;
                                mIntelligence = data[11];
                                mDirection = data[12];
                            } else if (length == 5) {


                                int type = data[3] & 0xFF;

                                System.out.println("type = " + type);

                                if (type == 133) {
                                    mTvContent.setText("");
                                    mTvContent.append("数据个数：" + length + "\n");
                                    mTvContent.append("暂停data：" + s + "\n");
                                    mTvContent.append("————暂停————\n");
                                    ToastUtils.SimpleToast("————暂停————");
                                } else if (type == 134) {
                                    mTvContent.setText("");
                                    mTvContent.append("数据个数：" + length + "\n");
                                    mTvContent.append("停止data：" + s + "\n");
                                    mTvContent.append("————停止————\n");
                                    ToastUtils.SimpleToast("————停止————");

                                }


                            } else if (length == 18) {

                                int type = data[16] & 0xFF;
                                if (type == 134) {
                                    mTvContent.setText("");
                                    mTvContent.append("数据个数：" + length + "\n");
                                    mTvContent.append("停止data：" + s + "\n");
                                    mTvContent.append("————停止————\n");
                                    ToastUtils.SimpleToast("————停止————");

                                }
                            }


                            dealData();
                        } else {

                            if (data[0] == 111) {
                                //  o
                                //地址帧发送成功，接下来发送数据包
//                            System.out.println("地址帧发送成功");
//                            System.out.println("开始发送数据包");
                                isSendDizhiZhen = false;
                                isSendShujubao = true;
                                sendIndexShujubao();
                            } else if (data[0] == 107) {
                                //  k
                                //数据包发送成功，接下来发送地址帧
//                            System.out.println("数据包发送成功");
//                            System.out.println("开始发送地址帧");
                                index++;
                                if (index >= mCount) {
                                    System.out.println("数据发送结束！！");
                                    isSendStart = false;
                                    index = 0;
                                    isSendDizhiZhen = false;
                                    isSendShujubao = false;
                                    System.out.println("当前index = " + index + ",count = " + mCount);
                                    System.out.println("复位");
                                    sendFuweizhen();

                                    //停止计时器，进度设为100
                                    mCountDownTimer.cancel();
                                    mLoadbar.setProgress(100);

                                } else {
                                    isSendDizhiZhen = true;
                                    isSendShujubao = false;
                                    sendIndexDizhizhen();
                                }


                            } else if (data[0] == 120) {
                                // x
                                //发送失败
//                            index = 0;
//                            isSendDizhiZhen = false;
//                            isSendShujubao = false;
//                            isSendStart = false;
                                if (isChanpinZhen) {
                                    System.out.println("产品帧：文件不匹配");
                                    System.out.println("不予升级！");
                                } else if (isSendDizhiZhen || isSendShujubao) {
                                    //地址帧或者数据包出错的情况下
                                    System.out.println("地址帧或者数据包出错，重新发送");
                                    int i = index * 256 / 4096;
                                    index = i * 4096 / 256;
                                    isSendDizhiZhen = true;
                                    isSendShujubao = false;
                                    sendDizhizhen();
                                }
                            } else if (data[0] == 60) {
                                //<
                                if (isBanbenZhen) {
                                    System.out.println("版本帧：升级固件版本：旧");
                                    System.out.println("不予升级！");
                                }


                            } else if (data[0] == 61) {
                                //=
                                if (isChanpinZhen) {
                                    System.out.println("产品帧：文件匹配");
                                    System.out.println("开始发送版本帧：");
                                    sendBanbenzhen();

                                } else if (isBanbenZhen) {
                                    isSendStart = true;
                                    isSendStart = false;
                                    System.out.println("版本帧：升级固件版本：相同");
                                }


                            } else if (data[0] == 62) {
                                //>
                                if (isBanbenZhen) {
                                    System.out.println("版本帧：升级固件版本：新");
                                    System.out.println("开始升级：");
                                    sendDizhizhen();
                                }


                            } else if (data[0] == 42) {
                                //*
                                System.out.println("设备返回*，自动升级！！！");
                            }

                        }
                    }
                });
    }


}
