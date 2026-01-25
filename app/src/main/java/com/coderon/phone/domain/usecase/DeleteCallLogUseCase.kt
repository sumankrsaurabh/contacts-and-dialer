package com.coderon.phone.domain.usecase

import com.coderon.phone.data.model.CallLog
import com.coderon.phone.domain.repository.CallLogRepository

class DeleteCallLogUseCase(private val callLogRepository: CallLogRepository) {
    suspend operator fun invoke(callLog: CallLog) = callLogRepository.deleteCallLog(callLog)
}
