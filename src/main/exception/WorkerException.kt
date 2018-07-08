package exception

class WorkerException(previous: Exception) : Exception("A worker Exception has occured.", previous) {
}