package com.rebirthCorp.rebirth.models.common

// a central point of definition for views allow nested objects in models
// todo port this fix to our other spring projects
object View {
    interface Summary
    interface Detailed : Summary
    interface Private : Detailed
}