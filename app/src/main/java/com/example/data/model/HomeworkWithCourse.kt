package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class HomeworkWithCourse(
    @Embedded
    val homework: Homework,

    @Relation(
        parentColumn = "courseId",
        entityColumn = "id"
    )
    val course: Course?
)
