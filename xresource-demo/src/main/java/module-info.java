module org.xresource.demo {

    exports org.xresource.demo;

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
    requires spring.security.crypto;
    requires jjwt.api;
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
    requires org.slf4j;
    requires static lombok;
    requires java.management;
    requires org.xresource;

    opens org.xresource.demo.entity to spring.core, spring.beans, spring.context, spring.web, spring.boot.autoconfigure,
            hibernate.core, jackson.databind, org.hibernate.orm.core, org.xresource, com.fasterxml.jackson.databind;
    opens org.xresource.demo.repository
            to spring.core, spring.beans, spring.context, spring.web, spring.boot.autoconfigure,
            hibernate.core, jackson.databind, org.xresource;
    opens org.xresource.demo.services to spring.core, spring.beans;
    opens org.xresource.demo.dto to com.fasterxml.jackson.databind;
    opens org.xresource.demo.config to spring.core, spring.beans, spring.context;
    opens org.xresource.demo.controller to spring.core,
            spring.beans, spring.web, spring.boot.autoconfigure,
            hibernate.core, jackson.databind, org.hibernate.orm.core, com.fasterxml.jackson.databind;
    opens org.xresource.demo.cron to spring.core, spring.beans;
    opens org.xresource.demo.auth to spring.core, spring.beans;
    opens org.xresource.demo.hooks to spring.core, spring.beans;
    opens org.xresource.demo.validations to spring.core, spring.beans;
    opens org.xresource.demo.security to
            spring.core,
            spring.beans,
            spring.context,
            spring.web,
            spring.webmvc,
            spring.boot,
            spring.boot.autoconfigure,
            spring.security.core,
            spring.security.config,
            spring.security.web,
            spring.aop,
            org.xresource,
            com.fasterxml.jackson.databind;

}
