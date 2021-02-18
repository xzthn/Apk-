package com.realsil.api;

import java.util.List;

/**
 * Desc: TODO
 * <p>
 * Author: meijie
 * PackageName: com.realsil.api
 * ProjectName: APKCheck
 * Date: 2018/12/7 9:26
 */
public class UpgradeResp {

    private int retCode; // 0 成功
    private String retMsg;
    private boolean success;
    private List<UpgradeListBean> upgradeList;

    public int getRetCode() { return retCode;}

    public void setRetCode(int retCode) { this.retCode = retCode;}

    public String getRetMsg() { return retMsg;}

    public void setRetMsg(String retMsg) { this.retMsg = retMsg;}

    public boolean isSuccess() { return success;}

    public void setSuccess(boolean success) { this.success = success;}

    public List<UpgradeListBean> getUpgradeList() { return upgradeList;}

    public void setUpgradeList(List<UpgradeListBean> upgradeList) { this.upgradeList = upgradeList;}

    public static class UpgradeListBean {

        private int id;
        private String upgradeTypeName;
        private String desc;
        private String version;
        private Object action;
        private String downloadUrl;
        private String checkCode;
        private String maxVersion;
        private String minVersion;
        private String dstChannel; // 遥控器渠道号

        public int getId() { return id;}

        public void setId(int id) { this.id = id;}

        public String getUpgradeTypeName() { return upgradeTypeName;}

        public void setUpgradeTypeName(String upgradeTypeName) { this.upgradeTypeName = upgradeTypeName;}

        public String getDesc() { return desc;}

        public void setDesc(String desc) { this.desc = desc;}

        public String getVersion() { return version;}

        public void setVersion(String version) { this.version = version;}

        public Object getAction() { return action;}

        public void setAction(Object action) { this.action = action;}

        public String getDownloadUrl() { return downloadUrl;}

        public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl;}

        public String getCheckCode() { return checkCode;}

        public void setCheckCode(String checkCode) { this.checkCode = checkCode;}

        public String getMaxVersion() { return maxVersion;}

        public void setMaxVersion(String maxVersion) { this.maxVersion = maxVersion;}

        public String getMinVersion() { return minVersion;}

        public void setMinVersion(String minVersion) { this.minVersion = minVersion;}

        public String getDstChannel() {
            return dstChannel;
        }

        public void setDstChannel(String dstChannel) {
            this.dstChannel = dstChannel;
        }

        @Override
        public String toString() {
            return "UpgradeListBean{" +
                    "id=" + id +
                    ", upgradeTypeName='" + upgradeTypeName + '\'' +
                    ", desc='" + desc + '\'' +
                    ", version='" + version + '\'' +
                    ", action=" + action +
                    ", downloadUrl='" + downloadUrl + '\'' +
                    ", checkCode='" + checkCode + '\'' +
                    ", maxVersion='" + maxVersion + '\'' +
                    ", minVersion='" + minVersion + '\'' +
                    ", dstChannel='" + dstChannel + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "UpgradeResp{" +
                "retCode=" + retCode +
                ", retMsg='" + retMsg + '\'' +
                ", success=" + success +
                ", upgradeList=" + upgradeList +
                '}';
    }
}
