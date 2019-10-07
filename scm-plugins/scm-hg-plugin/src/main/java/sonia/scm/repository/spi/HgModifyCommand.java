package sonia.scm.repository.spi;

import com.aragost.javahg.Changeset;
import com.aragost.javahg.Repository;
import com.aragost.javahg.commands.CommitCommand;
import com.aragost.javahg.commands.ExecutionException;
import com.aragost.javahg.commands.PullCommand;
import com.aragost.javahg.commands.RemoveCommand;
import com.aragost.javahg.commands.StatusCommand;
import org.apache.commons.lang.StringUtils;
import sonia.scm.ContextEntry;
import sonia.scm.NoChangesMadeException;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.util.WorkingCopy;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static sonia.scm.AlreadyExistsException.alreadyExists;
import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

public class HgModifyCommand implements ModifyCommand {

  private HgCommandContext context;
  private final HgWorkdirFactory workdirFactory;

  public HgModifyCommand(HgCommandContext context, HgWorkdirFactory workdirFactory) {
    this.context = context;
    this.workdirFactory = workdirFactory;
  }

  @Override
  public String execute(ModifyCommandRequest request) {

    try (WorkingCopy<com.aragost.javahg.Repository> workingCopy = workdirFactory.createWorkingCopy(context, request.getBranch())) {
      Repository workingRepository = workingCopy.getWorkingRepository();
      request.getRequests().forEach(
        partialRequest -> {
          try {
            partialRequest.execute(new Worker() {
              @Override
              public void delete(String toBeDeleted) {
                RemoveCommand.on(workingRepository).execute(toBeDeleted);
              }

              @Override
              public void create(String toBeCreated, File file, boolean overwrite) throws IOException {
                Path targetFile = new File(workingRepository.getDirectory(), toBeCreated).toPath();
                createDirectories(targetFile);
                if (overwrite) {
                  Files.move(file.toPath(), targetFile, REPLACE_EXISTING);
                } else {
                  try {
                    Files.move(file.toPath(), targetFile);
                  } catch (FileAlreadyExistsException e) {
                    throw alreadyExists(createFileContext(toBeCreated));
                  }
                }
                try {
                  addFileToHg(targetFile.toFile());
                } catch (ExecutionException e) {
                  throwInternalRepositoryException("could not add new file to index", e);
                }
              }

              @Override
              public void modify(String path, File file) throws IOException {
                Path targetFile = new File(workingRepository.getDirectory(), path).toPath();
                createDirectories(targetFile);
                if (!targetFile.toFile().exists()) {
                  throw notFound(createFileContext(path));
                }
                Files.move(file.toPath(), targetFile, REPLACE_EXISTING);
                try {
                  addFileToHg(targetFile.toFile());
                } catch (ExecutionException e) {
                  throwInternalRepositoryException("could not modify existing file", e);
                }
              }

              @Override
              public void move(String sourcePath, String targetPath) {
              }

              private void createDirectories(Path targetFile) throws IOException {
                try {
                  Files.createDirectories(targetFile.getParent());
                } catch (FileAlreadyExistsException e) {
                  throw alreadyExists(createFileContext(targetFile.toString()));
                }
              }

              private ContextEntry.ContextBuilder createFileContext(String path) {
                ContextEntry.ContextBuilder contextBuilder = entity("file", path);
                if (!StringUtils.isEmpty(request.getBranch())) {
                  contextBuilder.in("branch", request.getBranch());
                }
                contextBuilder.in(context.getScmRepository());
                return contextBuilder;
              }

              private void addFileToHg(File file) {
                workingRepository.workingCopy().add(file.getAbsolutePath());
              }
            });
          } catch (IOException e) {
            throwInternalRepositoryException("could not execute command on repository", e);
          }
        }
      );
      if (StatusCommand.on(workingRepository).lines().isEmpty()) {
        throw new NoChangesMadeException(context.getScmRepository());
      }
      CommitCommand.on(workingRepository).user(String.format("%s <%s>", request.getAuthor().getName(), request.getAuthor().getMail())).message(request.getCommitMessage()).execute();
      List<Changeset> execute = pullModifyChangesToCentralRepository(request, workingCopy);
      return execute.get(0).getNode();
    } catch (ExecutionException e) {
      throwInternalRepositoryException("could not execute command on repository", e);
      return null;
    }
  }

  private List<Changeset> pullModifyChangesToCentralRepository(ModifyCommandRequest request, WorkingCopy<com.aragost.javahg.Repository> workingCopy) {
    try {
      com.aragost.javahg.commands.PullCommand pullCommand = PullCommand.on(workingCopy.getCentralRepository());
      workdirFactory.configure(pullCommand);
      return pullCommand.execute(workingCopy.getDirectory().getAbsolutePath());
    } catch (Exception e) {
      throw new IntegrateChangesFromWorkdirException(context.getScmRepository(),
        String.format("Could not pull modify changes from working copy to central repository", request.getBranch()),
        e);
    }
  }

  private String throwInternalRepositoryException(String message, Exception e) {
    throw new InternalRepositoryException(context.getScmRepository(), message, e);
  }
}
