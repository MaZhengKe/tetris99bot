package my.mk.serial;


import lombok.Data;

@Data
public class ParamConfig {

    /**
     * 串口号
     */
    private String serialNumber;
    /**
     * 波特率
     */
    private int baudRate;
    /**
     * 校验位
     */
    private int checkoutBit;
    /**
     * 数据位
     */
    private int dataBit;
    /**
     * 停止位
     */
    private int stopBit;

    /**
     * 构造方法
     *
     * @param serialNumber 串口号
     * @param baudRate     波特率
     * @param checkoutBit  校验位
     * @param dataBit      数据位
     * @param stopBit      停止位
     */
    public ParamConfig(String serialNumber, int baudRate, int checkoutBit, int dataBit, int stopBit) {
        this.serialNumber = serialNumber;
        this.baudRate = baudRate;
        this.checkoutBit = checkoutBit;
        this.dataBit = dataBit;
        this.stopBit = stopBit;
    }
}