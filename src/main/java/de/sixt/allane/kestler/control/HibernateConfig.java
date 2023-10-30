package de.sixt.allane.kestler.control;

import de.sixt.allane.kestler.model.Contract;
import de.sixt.allane.kestler.model.Customer;
import de.sixt.allane.kestler.model.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import java.util.Properties;

@Configuration
public class HibernateConfig {

    @Autowired
    private Environment env;

    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        System.out.println("created session factory");
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setHibernateProperties(getHibernateProperties());
        sessionFactory.setAnnotatedClasses(Customer.class, Vehicle.class, Contract.class);
        return sessionFactory;
    }

    private Properties getHibernateProperties() {
        System.out.println("created getHibernateProperties");
        Properties properties = new Properties();

        properties.setProperty(org.hibernate.cfg.Environment.URL, env.getProperty("spring.datasource.url"));
        properties.setProperty(org.hibernate.cfg.Environment.USER, env.getProperty("spring.datasource.username"));
        properties.setProperty(org.hibernate.cfg.Environment.PASS, env.getProperty("spring.datasource.password"));

        properties.setProperty(org.hibernate.cfg.Environment.DRIVER, env.getProperty("spring.datasource.driver-class-name"));
        properties.setProperty(org.hibernate.cfg.Environment.DIALECT, env.getProperty("spring.jpa.database-platform"));
        properties.setProperty(org.hibernate.cfg.Environment.HBM2DDL_AUTO, env.getProperty("spring.jpa.hibernate.ddl-auto"));

//        properties.put(Environment.SHOW_SQL, "true");
//        properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
        return properties;
    }
}
