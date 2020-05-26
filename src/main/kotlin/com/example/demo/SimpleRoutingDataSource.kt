package com.example.demo

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import org.springframework.transaction.support.TransactionSynchronizationManager

class SimpleRoutingDataSource : AbstractRoutingDataSource() {
    companion object {
        const val MASTER = "master"
        const val REPLICA = "replica"
    }

    override fun determineCurrentLookupKey(): String? {
        return REPLICA.takeIf { TransactionSynchronizationManager.isCurrentTransactionReadOnly() }
    }
}