package cai.lib.kvstore.annotations

/**
 * 忽略Store中的字段
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class FieldIgnore