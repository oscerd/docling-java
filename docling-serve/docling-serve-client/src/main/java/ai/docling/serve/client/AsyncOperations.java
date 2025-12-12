package ai.docling.serve.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.docling.serve.api.DoclingServeAsyncApi;
import ai.docling.serve.api.async.request.AsyncChunkDocumentRequest;
import ai.docling.serve.api.async.request.AsyncConvertDocumentRequest;
import ai.docling.serve.api.chunk.request.options.HierarchicalChunkerOptions;
import ai.docling.serve.api.chunk.request.options.HybridChunkerOptions;
import ai.docling.serve.api.chunk.response.ChunkDocumentResponse;
import ai.docling.serve.api.convert.response.ConvertDocumentResponse;
import ai.docling.serve.api.task.request.TaskResultRequest;
import ai.docling.serve.api.task.request.TaskStatusPollRequest;
import ai.docling.serve.api.task.response.TaskStatus;
import ai.docling.serve.api.task.response.TaskStatusPollResponse;
import ai.docling.serve.api.util.ValidationUtils;

/**
 * Implementation of asynchronous document conversion and chunking operations.
 *
 * <p>This class provides methods to initiate async conversions and chunking operations
 * that return immediately with a task ID. It also provides convenience methods that
 * handle polling automatically until completion.
 */
final class AsyncOperations implements DoclingServeAsyncApi {
  private static final Logger LOG = LoggerFactory.getLogger(AsyncOperations.class);

  private final HttpOperations httpOperations;
  private final TaskOperations taskOperations;

  AsyncOperations(HttpOperations httpOperations, TaskOperations taskOperations) {
    this.httpOperations = httpOperations;
    this.taskOperations = taskOperations;
  }

  @Override
  public TaskStatusPollResponse convertSourceAsync(AsyncConvertDocumentRequest request) {
    ValidationUtils.ensureNotNull(request, "request");
    return this.httpOperations.executePost("/v1/convert/source/async", request, TaskStatusPollResponse.class);
  }

  @Override
  public ConvertDocumentResponse convertSourceAsyncAndWait(AsyncConvertDocumentRequest request) {
    ValidationUtils.ensureNotNull(request, "request");

    var taskResponse = convertSourceAsync(request);
    var taskId = taskResponse.getTaskId();

    LOG.info("Started async conversion with task ID: {}", taskId);

    var completedStatus = pollUntilComplete(taskId, request.getPollInterval().toMillis(), request.getTimeout().toMillis());

    LOG.info("Task {} completed successfully", taskId);
    return this.taskOperations.convertTaskResult(TaskResultRequest.builder().taskId(taskId).build());
  }

  @Override
  public TaskStatusPollResponse chunkSourceWithHierarchicalChunkerAsync(AsyncChunkDocumentRequest request) {
    ValidationUtils.ensureNotNull(request, "request");
    validateChunkerOptions(request, HierarchicalChunkerOptions.class);
    return this.httpOperations.executePost("/v1/chunk/hierarchical/source/async", request, TaskStatusPollResponse.class);
  }

  @Override
  public ChunkDocumentResponse chunkSourceWithHierarchicalChunkerAsyncAndWait(AsyncChunkDocumentRequest request) {
    ValidationUtils.ensureNotNull(request, "request");
    validateChunkerOptions(request, HierarchicalChunkerOptions.class);

    var taskResponse = chunkSourceWithHierarchicalChunkerAsync(request);
    var taskId = taskResponse.getTaskId();

    LOG.info("Started async hierarchical chunking with task ID: {}", taskId);

    var completedStatus = pollUntilComplete(taskId, request.getPollInterval().toMillis(), request.getTimeout().toMillis());

    LOG.info("Task {} completed successfully", taskId);
    return this.taskOperations.chunkTaskResult(TaskResultRequest.builder().taskId(taskId).build());
  }

  @Override
  public TaskStatusPollResponse chunkSourceWithHybridChunkerAsync(AsyncChunkDocumentRequest request) {
    ValidationUtils.ensureNotNull(request, "request");
    validateChunkerOptions(request, HybridChunkerOptions.class);
    return this.httpOperations.executePost("/v1/chunk/hybrid/source/async", request, TaskStatusPollResponse.class);
  }

  @Override
  public ChunkDocumentResponse chunkSourceWithHybridChunkerAsyncAndWait(AsyncChunkDocumentRequest request) {
    ValidationUtils.ensureNotNull(request, "request");
    validateChunkerOptions(request, HybridChunkerOptions.class);

    var taskResponse = chunkSourceWithHybridChunkerAsync(request);
    var taskId = taskResponse.getTaskId();

    LOG.info("Started async hybrid chunking with task ID: {}", taskId);

    var completedStatus = pollUntilComplete(taskId, request.getPollInterval().toMillis(), request.getTimeout().toMillis());

    LOG.info("Task {} completed successfully", taskId);
    return this.taskOperations.chunkTaskResult(TaskResultRequest.builder().taskId(taskId).build());
  }

  private void validateChunkerOptions(AsyncChunkDocumentRequest request, Class<?> expectedOptionsType) {
    if (request.getChunkingOptions() != null && !expectedOptionsType.isInstance(request.getChunkingOptions())) {
      throw new IllegalArgumentException(
          "Chunking options must be of type " + expectedOptionsType.getSimpleName() +
          " for this operation, but got " + request.getChunkingOptions().getClass().getSimpleName());
    }
  }

  private TaskStatusPollResponse pollUntilComplete(String taskId, long pollIntervalMs, long timeoutMs) {
    long startTime = System.currentTimeMillis();
    long deadline = startTime + timeoutMs;

    while (System.currentTimeMillis() < deadline) {
      var pollRequest = TaskStatusPollRequest.builder().taskId(taskId).build();
      var status = this.taskOperations.pollTaskStatus(pollRequest);

      LOG.debug("Task {} status: {}", taskId, status.getTaskStatus());

      if (status.getTaskStatus() == TaskStatus.SUCCESS) {
        return status;
      } else if (status.getTaskStatus() == TaskStatus.FAILURE) {
        String errorMessage = extractErrorMessage(status);
        throw new DoclingServeClientException(
            "Async operation failed for task " + taskId + ": " + errorMessage,
            500,
            errorMessage
        );
      }

      // Wait before next poll
      try {
        Thread.sleep(pollIntervalMs);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Async operation interrupted for task " + taskId, e);
      }
    }

    throw new RuntimeException("Async operation timed out after " + timeoutMs + "ms for task: " + taskId);
  }

  private String extractErrorMessage(TaskStatusPollResponse status) {
    if (status.getTaskStatusMetadata() != null) {
      // Build error message from metadata if available
      var meta = status.getTaskStatusMetadata();
      if (meta.getNumFailed() != null && meta.getNumFailed() > 0) {
        return "Task failed: " + meta.getNumFailed() + " document(s) failed processing";
      }
    }
    return "Task failed without detailed error message";
  }
}
