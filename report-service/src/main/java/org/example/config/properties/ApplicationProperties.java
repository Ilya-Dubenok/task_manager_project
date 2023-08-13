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

        public UserService getUserService() {
            return userService;
        }

        public void setUserService(UserService userService) {
            this.userService = userService;
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

    }

}
