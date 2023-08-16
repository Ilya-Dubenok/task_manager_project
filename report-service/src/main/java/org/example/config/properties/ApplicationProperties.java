package org.example.config.properties;

public class ApplicationProperties {


    private NetworkProp network;


    public NetworkProp getNetwork() {
        return network;
    }

    public void setNetwork(NetworkProp network) {
        this.network = network;
    }


    public static class NetworkProp {


        private UserService userService;

        private AuditService auditService;

        private Minio minio;

        public UserService getUserService() {
            return userService;
        }

        public void setUserService(UserService userService) {
            this.userService = userService;
        }

        public AuditService getAuditService() {
            return auditService;
        }

        public void setAuditService(AuditService auditService) {
            this.auditService = auditService;
        }

        public Minio getMinio() {
            return minio;
        }

        public void setMinio(Minio minio) {
            this.minio = minio;
        }

        public static class UserService{

            private String address;

            private String host;

            private String appendix;

            private String internalAppendix;

            public String getAddress() {
                return address;
            }

            public void setAddress(String address) {
                this.address = address;
            }

            public String getHost() {
                return host;
            }

            public void setHost(String host) {
                this.host = host;
            }

            public String getAppendix() {
                return appendix;
            }

            public void setAppendix(String appendix) {
                this.appendix = appendix;
            }

            public String getInternalAppendix() {
                return internalAppendix;
            }

            public void setInternalAppendix(String internalAppendix) {
                this.internalAppendix = internalAppendix;
            }
        }

        public static class AuditService{

            private String address;

            private String host;

            private String appendix;

            private String internalAppendix;

            public String getAddress() {
                return address;
            }

            public void setAddress(String address) {
                this.address = address;
            }

            public String getHost() {
                return host;
            }

            public void setHost(String host) {
                this.host = host;
            }

            public String getAppendix() {
                return appendix;
            }

            public void setAppendix(String appendix) {
                this.appendix = appendix;
            }

            public String getInternalAppendix() {
                return internalAppendix;
            }

            public void setInternalAppendix(String internalAppendix) {
                this.internalAppendix = internalAppendix;
            }
        }

        public static class Minio{

            private String host;

            private String accessKey;

            private String secretKey;

            public String getHost() {
                return host;
            }

            public void setHost(String host) {
                this.host = host;
            }

            public String getAccessKey() {
                return accessKey;
            }

            public void setAccessKey(String accessKey) {
                this.accessKey = accessKey;
            }

            public String getSecretKey() {
                return secretKey;
            }

            public void setSecretKey(String secretKey) {
                this.secretKey = secretKey;
            }
        }

    }

}
