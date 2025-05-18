module org.xresource {
    exports org.xresource.core.actions;
    exports org.xresource.core.annotations;
    exports org.xresource.core.auth;
    exports org.xresource.core.cron;
    exports org.xresource.core.hook;
    exports org.xresource.core.logging;
    exports org.xresource.core.exception;
    exports org.xresource.core.response;
    exports org.xresource.core.service;
    exports org.xresource.core.util;
    exports org.xresource.core.validation;

    requires spring.boot.autoconfigure;
    requires spring.boot;
    requires spring.context;
    requires spring.beans;
    requires spring.core;
    requires spring.web;
    requires spring.webmvc;
    requires spring.aop;
    requires spring.security.config;
    requires spring.security.web;
    requires spring.security.core;
    requires spring.data.jpa;
    requires jakarta.persistence;
    requires jakarta.validation;
    requires jakarta.transaction;
    requires jakarta.annotation;
    requires spring.data.commons;
    requires jakarta.servlet;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.hibernate6;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.springdoc.openapi.ui;
    requires io.swagger.v3.oas.annotations;
    requires jakarta.inject;
    requires org.slf4j;
    requires static lombok;
    requires java.management;

    opens org.xresource.core.service to spring.core;

    opens org.xresource.internal.controller
            to spring.core, spring.beans, spring.context, spring.web, spring.boot.autoconfigure,
            hibernate.core, jackson.databind;
    opens org.xresource.internal.context
            to spring.core, spring.beans, spring.context, spring.web, spring.boot.autoconfigure,
            hibernate.core, jackson.databind;
    opens org.xresource.internal.actions to spring.core, spring.beans;
    opens org.xresource.internal.auth to spring.core, spring.beans;
    opens org.xresource.internal.config to spring.core, spring.beans, spring.context;
    opens org.xresource.internal.cron to spring.core, spring.beans;
    opens org.xresource.internal.exception to spring.core, spring.beans, spring.web;
    opens org.xresource.internal.openapi to spring.boot, spring.core, spring.beans, spring.context;
    opens org.xresource.internal.query to spring.core, spring.beans;
    opens org.xresource.internal.util to spring.core, spring.beans;
    opens org.xresource.internal.models to jackson.databind;

}
