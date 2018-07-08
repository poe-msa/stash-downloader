class WorkerContext(initialRemoteId: String, val delayMsBetweenWorkloads: Long) {
    var currentChangeId = initialRemoteId
}