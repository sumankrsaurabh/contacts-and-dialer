package com.coderon.phone.data.repository

import com.coderon.phone.data.dao.VoicemailDao
import com.coderon.phone.data.model.Voicemail

class VoicemailRepository(private val voicemailDao: VoicemailDao) {

    suspend fun saveVoicemail(voicemail: Voicemail) {
        voicemailDao.insertVoicemail(voicemail)
    }

    suspend fun getVoicemails(): List<Voicemail> {
        return voicemailDao.getAllVoicemails()
    }

    suspend fun deleteVoicemail(voicemail: Voicemail) {
        voicemailDao.deleteVoicemail(voicemail)
    }
}
