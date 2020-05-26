package com.example.demo

import com.zaxxer.hikari.HikariDataSource
import org.apache.commons.dbcp2.BasicDataSource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import java.util.*
import javax.sql.DataSource

@Configuration
@EnableJpaRepositories(
        basePackages = [Sample.PACKAGE_ROOT], // Set the package path where to find repository.
        entityManagerFactoryRef = Sample.ENTITY_MANAGER,
        transactionManagerRef = Sample.TRANSACTION_MANAGER
)
class Sample {

    companion object {
        private const val DB_NAME = "sample"
        const val PACKAGE_ROOT = "com.example.demo"
        const val MASTER_PROP_PREFIX = "datasource.$DB_NAME.master"
        const val MASTER_DATA_SOURCE = "$DB_NAME-master-datasource"
        const val REPLICA_PROP_PREFIX = "datasource.$DB_NAME.replica"
        const val REPLICA_DATA_SOURCE = "$DB_NAME-replica-datasource"
        const val ROUTING_DATA_SOURCE = "$DB_NAME-routing-datasource"
        const val ENTITY_MANAGER = "$DB_NAME-entity-manager"
        const val TRANSACTION_MANAGER = "$DB_NAME-transaction-manager"
    }

    @Primary
    @Bean(ENTITY_MANAGER)
    fun entityManager(@Qualifier(ROUTING_DATA_SOURCE) dataSource: DataSource): LocalContainerEntityManagerFactoryBean {
        return LocalContainerEntityManagerFactoryBean().apply {
            this.dataSource = dataSource

            // Set the package path where to find entities.
            setPackagesToScan(PACKAGE_ROOT)

            Properties().apply {
                // This line is for batch processing. Uncomment it if you need.
                // It would be nice for performance improvement if you have to do tons of inserts.
                /*
                put("hibernate.jdbc.batch_size", 1000)
                put("hibernate.order_inserts", true)
                put("hibernate.order_updates", true)
                 */
                put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect")
            }.also {
                setJpaProperties(it)
            }


            jpaVendorAdapter = HibernateJpaVendorAdapter()
        }
    }

    @Primary
    @Bean(value = [MASTER_DATA_SOURCE])
    @ConfigurationProperties(prefix = MASTER_PROP_PREFIX)
    fun masterDataSource(): DataSource {
        // For DBCP2
        return DataSourceBuilder.create().type(BasicDataSource::class.java).build()
    }

    @Bean(value = [REPLICA_DATA_SOURCE])
    @ConfigurationProperties(prefix = REPLICA_PROP_PREFIX)
    fun replicaDataSource(): DataSource {
        // For Hikari
        return DataSourceBuilder.create().type(HikariDataSource::class.java).build()
    }

    @Bean(ROUTING_DATA_SOURCE)
    fun routingDataSource(@Qualifier(REPLICA_DATA_SOURCE) replicaDataSource: DataSource,
                          @Qualifier(MASTER_DATA_SOURCE) masterDataSource: DataSource): DataSource {
        return SimpleRoutingDataSource().apply {
            setDefaultTargetDataSource(masterDataSource)
            setTargetDataSources(
                    hashMapOf<Any, Any>(
                            SimpleRoutingDataSource.REPLICA to replicaDataSource,
                            SimpleRoutingDataSource.MASTER to masterDataSource
                    )
            )
            afterPropertiesSet()
        }.let {
            LazyConnectionDataSourceProxy(it)
        }
    }

    @Primary
    @Bean(TRANSACTION_MANAGER)
    fun transactionManager(
            @Qualifier(ENTITY_MANAGER) entityManager: LocalContainerEntityManagerFactoryBean
    ): PlatformTransactionManager {
        return JpaTransactionManager().apply {
            entityManagerFactory = entityManager.`object`
        }
    }

}