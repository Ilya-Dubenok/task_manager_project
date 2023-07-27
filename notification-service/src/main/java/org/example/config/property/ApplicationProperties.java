package org.example.config.property;


public class ApplicationProperties {

    private MailProp mail;


    public MailProp getMail() {
        return mail;
    }

    public void setMail(MailProp mail) {
        this.mail = mail;
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



}
