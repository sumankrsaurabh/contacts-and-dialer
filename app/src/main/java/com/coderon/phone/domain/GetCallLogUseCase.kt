package com.coderon.phone.domain

import com.coderon.phone.domain.repository.CallLogRepository

class GetCallLogsUseCase(private val repository: CallLogRepository) {
    suspend operator fun invoke() = repository.getCallLogs()
}
