package cai.lib.kvstore.annotations

/**
 * 重命名Store中的字段
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD)
annotation class FieldRename(val value: String = "")