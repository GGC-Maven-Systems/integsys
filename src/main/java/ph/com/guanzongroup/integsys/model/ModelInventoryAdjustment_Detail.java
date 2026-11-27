/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.integsys.model;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author User
 */
public class ModelInventoryAdjustment_Detail {

    private static final int MAX_INDEX = 22;

    public SimpleStringProperty index01 = new SimpleStringProperty("");
    public SimpleStringProperty index02 = new SimpleStringProperty("");
    public SimpleStringProperty index03 = new SimpleStringProperty("");
    public SimpleStringProperty index04 = new SimpleStringProperty("");
    public SimpleStringProperty index05 = new SimpleStringProperty("");
    public SimpleStringProperty index06 = new SimpleStringProperty("");
    public SimpleStringProperty index07 = new SimpleStringProperty("");
    public SimpleStringProperty index08 = new SimpleStringProperty("");
    public SimpleStringProperty index09 = new SimpleStringProperty("");
    public SimpleStringProperty index10 = new SimpleStringProperty("");
    public SimpleStringProperty index11 = new SimpleStringProperty("");
    public SimpleStringProperty index12 = new SimpleStringProperty("");
    public SimpleStringProperty index13 = new SimpleStringProperty("");
    public SimpleStringProperty index14 = new SimpleStringProperty("");
    public SimpleStringProperty index15 = new SimpleStringProperty("");
    public SimpleStringProperty index16 = new SimpleStringProperty("");
    public SimpleStringProperty index17 = new SimpleStringProperty("");
    public SimpleStringProperty index18 = new SimpleStringProperty("");
    public SimpleStringProperty index19 = new SimpleStringProperty("");
    public SimpleStringProperty index20 = new SimpleStringProperty("");
    public SimpleStringProperty index21 = new SimpleStringProperty("");
    public SimpleStringProperty index22 = new SimpleStringProperty("");

    /**
     * Fills index01..indexNN in order with the given values. Any index not
     * supplied stays as an empty string (never null).
     */
    private void init(String... values) {
        SimpleStringProperty[] props = new SimpleStringProperty[]{
            index01, index02, index03, index04, index05, index06, index07, index08, index09, index10, index11, index12, index13, index14, index15, index16, index17, index18, index19, index20, index21, index22
        };
        for (int i = 0; i < values.length && i < MAX_INDEX; i++) {
            props[i].set(values[i]);
        }
    }

    public ModelInventoryAdjustment_Detail(
            String index01,
            String index02,
            String index03,
            String index04,
            String index05,
            String index06,
            String index07) {
        init(index01, index02, index03, index04, index05, index06, index07);
    }

    public ModelInventoryAdjustment_Detail(
            String index01,
            String index02,
            String index03,
            String index04,
            String index05,
            String index06,
            String index07,
            String index08) {
        init(index01, index02, index03, index04, index05, index06, index07, index08);
    }

    public ModelInventoryAdjustment_Detail(
            String index01,
            String index02,
            String index03,
            String index04,
            String index05,
            String index06,
            String index07,
            String index08,
            String index09) {
        init(index01, index02, index03, index04, index05, index06, index07, index08, index09);
    }

    public ModelInventoryAdjustment_Detail(
            String index01,
            String index02,
            String index03,
            String index04,
            String index05,
            String index06,
            String index07,
            String index08,
            String index09,
            String index10) {
        init(index01, index02, index03, index04, index05, index06, index07, index08, index09, index10);
    }

    public ModelInventoryAdjustment_Detail(
            String index01,
            String index02,
            String index03,
            String index04,
            String index05,
            String index06,
            String index07,
            String index08,
            String index09,
            String index10,
            String index11) {
        init(index01, index02, index03, index04, index05, index06, index07, index08, index09, index10, index11);
    }

    public ModelInventoryAdjustment_Detail(
            String index01,
            String index02,
            String index03,
            String index04,
            String index05,
            String index06,
            String index07,
            String index08,
            String index09,
            String index10,
            String index11,
            String index12,
            String index13,
            String index14,
            String index15,
            String index16,
            String index17,
            String index18,
            String index19,
            String index20,
            String index21,
            String index22) {
        init(index01, index02, index03, index04, index05, index06, index07, index08, index09, index10, index11, index12, index13, index14, index15, index16, index17, index18, index19, index20, index21, index22);
    }

    public String getIndex01() {
        return index01.get();
    }

    public void setIndex01(String index01) {
        this.index01.set(index01);
    }

    public SimpleStringProperty index01Property() {
        return index01;
    }

    public String getIndex02() {
        return index02.get();
    }

    public void setIndex02(String index02) {
        this.index02.set(index02);
    }

    public SimpleStringProperty index02Property() {
        return index02;
    }

    public String getIndex03() {
        return index03.get();
    }

    public void setIndex03(String index03) {
        this.index03.set(index03);
    }

    public SimpleStringProperty index03Property() {
        return index03;
    }

    public String getIndex04() {
        return index04.get();
    }

    public void setIndex04(String index04) {
        this.index04.set(index04);
    }

    public SimpleStringProperty index04Property() {
        return index04;
    }

    public String getIndex05() {
        return index05.get();
    }

    public void setIndex05(String index05) {
        this.index05.set(index05);
    }

    public SimpleStringProperty index05Property() {
        return index05;
    }

    public String getIndex06() {
        return index06.get();
    }

    public void setIndex06(String index06) {
        this.index06.set(index06);
    }

    public SimpleStringProperty index06Property() {
        return index06;
    }

    public String getIndex07() {
        return index07.get();
    }

    public void setIndex07(String index07) {
        this.index07.set(index07);
    }

    public SimpleStringProperty index07Property() {
        return index07;
    }

    public String getIndex08() {
        return index08.get();
    }

    public void setIndex08(String index08) {
        this.index08.set(index08);
    }

    public SimpleStringProperty index08Property() {
        return index08;
    }

    public String getIndex09() {
        return index09.get();
    }

    public void setIndex09(String index09) {
        this.index09.set(index09);
    }

    public SimpleStringProperty index09Property() {
        return index09;
    }

    public String getIndex10() {
        return index10.get();
    }

    public void setIndex10(String index10) {
        this.index10.set(index10);
    }

    public SimpleStringProperty index10Property() {
        return index10;
    }

    public String getIndex11() {
        return index11.get();
    }

    public void setIndex11(String index11) {
        this.index11.set(index11);
    }

    public SimpleStringProperty index11Property() {
        return index11;
    }

    public String getIndex12() {
        return index12.get();
    }

    public void setIndex12(String index12) {
        this.index12.set(index12);
    }

    public SimpleStringProperty index12Property() {
        return index12;
    }

    public String getIndex13() {
        return index13.get();
    }

    public void setIndex13(String index13) {
        this.index13.set(index13);
    }

    public SimpleStringProperty index13Property() {
        return index13;
    }

    public String getIndex14() {
        return index14.get();
    }

    public void setIndex14(String index14) {
        this.index14.set(index14);
    }

    public SimpleStringProperty index14Property() {
        return index14;
    }

    public String getIndex15() {
        return index15.get();
    }

    public void setIndex15(String index15) {
        this.index15.set(index15);
    }

    public SimpleStringProperty index15Property() {
        return index15;
    }

    public String getIndex16() {
        return index16.get();
    }

    public void setIndex16(String index16) {
        this.index16.set(index16);
    }

    public SimpleStringProperty index16Property() {
        return index16;
    }

    public String getIndex17() {
        return index17.get();
    }

    public void setIndex17(String index17) {
        this.index17.set(index17);
    }

    public SimpleStringProperty index17Property() {
        return index17;
    }

    public String getIndex18() {
        return index18.get();
    }

    public void setIndex18(String index18) {
        this.index18.set(index18);
    }

    public SimpleStringProperty index18Property() {
        return index18;
    }

    public String getIndex19() {
        return index19.get();
    }

    public void setIndex19(String index19) {
        this.index19.set(index19);
    }

    public SimpleStringProperty index19Property() {
        return index19;
    }

    public String getIndex20() {
        return index20.get();
    }

    public void setIndex20(String index20) {
        this.index20.set(index20);
    }

    public SimpleStringProperty index20Property() {
        return index20;
    }

    public String getIndex21() {
        return index21.get();
    }

    public void setIndex21(String index21) {
        this.index21.set(index21);
    }

    public SimpleStringProperty index21Property() {
        return index21;
    }

    public String getIndex22() {
        return index22.get();
    }

    public void setIndex22(String index22) {
        this.index22.set(index22);
    }

    public SimpleStringProperty index22Property() {
        return index22;
    }

}
