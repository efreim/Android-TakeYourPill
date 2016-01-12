package pl.balazinski.jakub.takeyourpill.data;

/**
 * Created by Kuba on 12.01.2016.
 */
public class TemporaryPill {
    private Integer id;
    private String activeSub;
    private String name;
    private String count;
    private Integer barcodeNumber;
    private String price;

    public TemporaryPill(Integer id, String activeSub, String name, String count, Integer barcodeNumber, String price) {
        this.id = id;
        this.activeSub = activeSub;
        this.name = name;
        this.count = count;
        this.barcodeNumber = barcodeNumber;
        this.price = price;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getActiveSub() {
        return activeSub;
    }

    public void setActiveSub(String activeSub) {
        this.activeSub = activeSub;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public Integer getBarcodeNumber() {
        return barcodeNumber;
    }

    public void setBarcodeNumber(Integer barcodeNumber) {
        this.barcodeNumber = barcodeNumber;
    }
}
