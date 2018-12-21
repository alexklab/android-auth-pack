package com.demo.auth.firebase.data.entity

import com.demo.auth.firebase.data.repository.FirebaseAuthRepository
import com.google.firebase.auth.FirebaseUser

class FirebaseUserProfileFactory : FirebaseAuthRepository.Factory<UserProfile> {

    override fun create(user: FirebaseUser): UserProfile =
        UserProfile(
            uid = user.uid,
            name = user.displayName,
            email = user.email,
            logoUrl = user.photoUrl?.toString(),
            networkProvider = user.providerId
        )

}