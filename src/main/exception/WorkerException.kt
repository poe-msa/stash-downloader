package exception

class WorkerException(message: String?, previous: Exception?) : Exception("A worker Exception has occured.", previous) {
}