package io.github.phantamanta44.tiabot2.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.github.phantamanta44.discord4j.data.Permission;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {

    String name();

    String usage();

    String desc() default "No description provided.";

    String docs() default "";

    String[] aliases() default {};

    CmdPerm[] perms() default {};

    Permission[] dcPerms() default {};
    
    boolean guildOnly() default false;

}