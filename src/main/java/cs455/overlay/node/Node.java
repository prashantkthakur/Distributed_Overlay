package cs455.overlay.node;


public interface Node {
    void onEvent(byte[] data);
    void onEvent(String event, String socketInfo, Connections conn);
    void onEvent(String event, String socketHostIp);

}
