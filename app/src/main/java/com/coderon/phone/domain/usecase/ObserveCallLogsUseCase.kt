package com.coderon.phone.domain.usecase

import com.coderon.phone.data.model.CallLog
import com.coderon.phone.domain.repository.CallLogRepository
import kotlinx.coroutines.flow.Flow

class ObserveCallLogsUseCase(private val callLogRepository: CallLogRepository) {
    operator fun invoke(): Flow<List<CallLog>> = callLogRepository.observeCallLogs()
}
