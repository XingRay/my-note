# ddmlib获取Android设备信息

本节实例在maven工程配置
本节示例IDevice



InfPhnoe.java

public class InfPhone {
	private String phonebrand; //手机品牌
	private String phoneandroidversion; //手机信息的系统版本
	private String phonemodel; //机器型号
	private String phonecpu; //cpu版本
	private String phoneplatform; //主板平台
	private String phonefingerprint; //系统指纹 

	public String getPhonebrand() {
		return phonebrand;
	}
	public void setPhonebrand(String phonebrand) {
		this.phonebrand = phonebrand;
	}
	public String getPhoneandroidversion() {
		return phoneandroidversion;
	}
	public void setPhoneandroidversion(String phoneandroidversion) {
		this.phoneandroidversion = phoneandroidversion;
	}
	public String getPhonemodel() {
		return phonemodel;
	}
	public void setPhonemodel(String phonemodel) {
		this.phonemodel = phonemodel;
	}
	public String getPhonecpu() {
		return phonecpu;
	}
	public void setPhonecpu(String phonecpu) {
		this.phonecpu = phonecpu;
	}
	public String getPhoneplatform() {
		return phoneplatform;
	}
	public void setPhoneplatform(String phoneplatform) {
		this.phoneplatform = phoneplatform;
	}
	public String getPhonefingerprint() {
		return phonefingerprint;
	}
	public void setPhonefingerprint(String phonefingerprint) {
		this.phonefingerprint = phonefingerprint;
	}
}
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25
26
27
28
29
30
31
32
33
34
35
36
37
38
39
40
41
42
43
44
45
MyDevice.java

	/*
	 * 获取手机硬件信息
	 */
	public static InfPhone getPhoneInfo(IDevice iDevice) {
		InfPhone infPhone = new InfPhone();
		infPhone.setPhoneandroidversion(iDevice.getProperty("ro.build.version.release"));	
		infPhone.setPhonebrand(iDevice.getProperty("ro.product.brand"));
		infPhone.setPhonemodel(iDevice.getProperty("ro.product.model"));
		infPhone.setPhonecpu(iDevice.getProperty("ro.product.cpu.abi"));
		infPhone.setPhoneplatform(iDevice.getProperty("ro.board.platform"));
		infPhone.setPhonefingerprint(iDevice.getProperty("ro.build.fingerprint"));
	
		return infPhone;
	}
1
2
3
4
5
6
7
8
9
10
11
12
13
14
/*
* 一个简单的方法，它尝试通过{@link #getSystemProperty(String)}检索一个属性，
* 等待时间很短，并且可以处理异常。
* 注意:如果希望控制超时，最好使用{@link #getSystemProperty(String)}。
* @return值，如果属性值不是立即可用的，则返回null
*/
String getProperty(@NonNull String name);
1
2
3
4
5
6
7
App.java

private static AdbVersion AdbVersion;
    private static InfPhone phoneInfo;

	public static void main( String[] args )
	{
	    System.out.println( "Hello Springboot!" );
	    MyDevice mDevice = new MyDevice();
	    AdbVersion = mDevice.getAdbVersion();
	    System.out.println("当前adb版本: " + AdbVersion.toString());
	    for(IDevice iDevice : mDevice.iDevice()) {
	    	System.out.println("获取当前设备 " + iDevice);
	    	phoneInfo = MyDevice.getPhoneInfo(iDevice);
	    	String phonebrand = phoneInfo.getPhonebrand();
	    	String phoneandroidversion = phoneInfo.getPhoneandroidversion();
	    	String phonemodel = phoneInfo.getPhonemodel();
	    	String phonecpu = phoneInfo.getPhonecpu();
	    	String phoneplatform = phoneInfo.getPhoneplatform();
	    	String phonefingerprint = phoneInfo.getPhonefingerprint();
	    	System.out.println(phonebrand);
	    	System.out.println(phoneandroidversion);
	    	System.out.println(phonemodel);
	    	System.out.println(phonecpu);
	    	System.out.println(phoneplatform);
	    	System.out.println(phonefingerprint);
	    }    
	}
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25
26
运行结果：