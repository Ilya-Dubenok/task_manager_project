package org.example.config.property;


public class ApplicationProperties {

    private MailProp mail;

    private NetworkProp network;

    public MailProp getMail() {
        return mail;
    }

    public void setMail(MailProp mail) {
        this.mail = mail;
    }

    public NetworkProp getNetwork() {
        return network;
    }

    public void setNetwork(NetworkProp network) {
        this.network = network;
    }

    public static class MailProp {
        private String email;

        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class NetworkProp {

        private String host;

        private AuditService auditService;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public AuditService getAuditService() {
            return auditService;
        }

        public void setAuditService(AuditService auditService) {
            this.auditService = auditService;
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

    }

}
