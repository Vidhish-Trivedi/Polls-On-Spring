package com.mypolls.polls;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EntityScan(basePackageClasses = {
    PollsApplication.class,
    Jsr310JpaConverters.class           // JPA converters to use Java 8 Date/Time and store as SQL types in DB.
})
public class PollsApplication {

    // Set default time zone to UTC
    @PostConstruct
    void init(){
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

	public static void main(String[] args) {
		SpringApplication.run(PollsApplication.class, args);
	}

}
