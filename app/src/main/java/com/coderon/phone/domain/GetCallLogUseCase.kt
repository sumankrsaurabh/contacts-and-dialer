package com.coderon.phone.domain

class GetCallLogsUseCase(private val repository: CallLogRepository) {
    suspend operator fun invoke() = repository.getCallLogs()
}
