package tv.metaxy.dashboard;
public final class BrokerPolicy {
    private BrokerPolicy() { }
    public static boolean ownerAllowed(int caller,int owner){return owner>=10000&&caller==owner;}
    public static boolean packageAllowed(String value){return value!=null&&value.length()<200&&value.matches("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+");}
}
