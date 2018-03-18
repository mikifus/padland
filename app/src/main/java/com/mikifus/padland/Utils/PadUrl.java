package com.mikifus.padland.Utils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by mikifus on 16/03/17.
 */

public class PadUrl {

    private String name;
    private String server;
    private String prefix;

    private PadUrl(Builder builder) {
        this.name = builder.name;
        this.server = builder.server;
        this.prefix = builder.prefix;
    }

    public String getPadName() {
        return name;
    }

    public String getPadServer() {
        return server;
    }

    public String getPadPrefix() {
        return prefix;
    }

    public URL getUrl() throws MalformedURLException {
        return new URL(getString());
    }

    public String getString() {
        return makeBaseUrl() + name;
    }

    private String makeBaseUrl() {
        String local_prefix = prefix;
        if( local_prefix.isEmpty() ) {
            throw new RuntimeException("The pad url was not correctly built. Check the fconfiguration for this server ("+server+").");
        }
        // Must end with /
        if(!local_prefix.endsWith("/")) {
            local_prefix = local_prefix + "/";
        }
        return local_prefix;
    }

    @Override
    public String toString() {
        return getString();
    }

    public static class Builder {

        private String name;
        private String server;
        private String prefix;

        public Builder() {}

        public Builder padName(String name) {
            name = name.replaceAll(" ", "_");
            this.name = name;
            return this;
        }

        public Builder padServer(String server) {
            server = server.replaceAll("/$", ""); // Remove trailing slash
            this.server = server;
            return this;
        }

        public Builder padPrefix(String prefix) {
            server = server.replaceAll("/$", ""); // Remove trailing slash
            this.prefix = prefix;
            return this;
        }

        public PadUrl build() {
            return new PadUrl(this);
        }
    }
}
