package org.example.config.property;


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

        private NotificationService notificationService;

        public UserService getUserService() {
            return userService;
        }

        public void setUserService(UserService userService) {
            this.userService = userService;
        }

        public AuditService getAuditService() {
            return auditService;
        }

        public NotificationService getNotificationService() {
            return notificationService;
        }

        public void setNotificationService(NotificationService notificationService) {
            this.notificationService = notificationService;
        }

        public void setAuditService(AuditService auditService) {
            this.auditService = auditService;
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
        }

        public static class NotificationService{

            private String address;

            private String host;

            private String appendix;

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

        }

    }

}
