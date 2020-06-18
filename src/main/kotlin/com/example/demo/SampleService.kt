package com.example.demo

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Transactional(
        transactionManager = Sample.TRANSACTION_MANAGER,
        propagation = Propagation.REQUIRES_NEW,
        readOnly = true
)
@Service
class SampleService(
        private val tbUserRepository: TbUserRepository
) {

    fun getData(): List<String> {
        return tbUserRepository.findAll().map {
            it.userId
        }
    }

    @Transactional
    fun saveData(entity: TbUser) {
        tbUserRepository.save(entity)
    }

}