package com.lzui.apkupgrade.bean;

/**
 * 作者:meijie
 * 包名:com.lzui.apkupgrade.bean
 * 工程名:APKCheck
 * 时间:2018/8/1 14:32
 * 说明: 应用升级，LauncherAppUpgrade 的 extra 字段对应的实体
 */
public class ExtraBean {

    private String action; // 需要启动的服务的  action
    private String className; // 服务完整路径
    private int reboot; // 重启盒子

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getReboot() {
        return reboot;
    }

    public void setReboot(int reboot) {
        this.reboot = reboot;
    }

    @Override
    public String toString() {
        return "ExtraBean{" +
                "action='" + action + '\'' +
                ", className='" + className + '\'' +
                ", reboot=" + reboot +
                '}';
    }
}
