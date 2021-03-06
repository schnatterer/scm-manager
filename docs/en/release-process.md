# How to release SCM-Manager v2 core


To release a new version of SCM-Manager v2 you have to do the following steps (replace placeholders `<version>` accordingly, eg. with `2.1.0`):

## Check out default branch

Make sure you have no changes you want to keep!

```
git fetch && git checkout -f origin/develop && git clean -fd && git checkout -B develop
```

## Merge support branch

Check whether there is an integration branch for the previous release or bugfixes not merged into the develop branch. Merge them now.

```
git merge origin/support/<support branch>
```

## Modify Changelog

Change "Unreleased" header in `CHANGELOG.md` to  `<version> - <current date>`

## Create release branch

`git checkout -b release/<version>`

## Commit version changes

```
git add CHANGELOG.md
git commit -m "Adjust changelog for release <version>"
```

## Push release branch

`git push origin release/<version>`

## Wait for Jenkins build

Jenkins will

- update `pom.xml` and `package.json`
- merge with master branch
- build and deploy everything
- set the new development version for the develop branch
- delete the release branch

## Make a party

# How to release SCM-Manager v2 plugins

To release a new version of a Plugin for SCM-Manager v2 you have to do the following steps (replace placeholder `<version>` accordingly, eg. with `2.1.0`):

## Check out default branch

Make sure you have no changes you want to keep!

```
git fetch && git checkout -f origin/develop && git clean -fd && git checkout -B develop
```

## Merge support branch

Check whether there is an integration branch for the previous release or bugfixes not merged into the develop branch. Merge them now.

```
git merge origin/support/<support branch>
```

## Update SCM parent if necessary

If you need to update the parent of the plugin to a new release of SCM-Manager, change it now:

- `pom.xml`: `parent.version`
- `package.json`: `dependencies.ui-plugins`

## Plugin dependencies

Check if all plugin dependencies are proper versions and not SNAPSHOT!

## Build, commit and push

```
rm -rf node_modules yarn.lock && mvn clean install \
&& git add yarn.lock pom.xml package.json \
&& git commit -m "Update to new version of SCM-Manager" \
&& git push origin develop
```

Wait for Jenkins to be green.

## Modify Changelog

Change "Unreleased" header in `CHANGELOG.md` to  `<version> - <current date>`

## Create, commit and push release branch

```
export VERSION=<version> \
&& git checkout -b release/$VERSION \
&& git commit -am "Prepare release of $VERSION" \
&& git push origin release/$VERSION
```

## Wait for Jenkins build

Jenkins will

- update versions in pom.xml and package.json
- merge with master branch
- build and deploy everything
- set the new development version for the develop branch
- delete the release branch

## Attention: Creating new plugins
If you are creating a new plugin which doesn't exist in the SCM-Manager Plugin-Center yet, your plugin will not be shown after the release. First you have to create a `index.md` in the Plugin-Center Repository. 

Example: https://github.com/scm-manager/website/blob/master/content/plugins/scm-teamscale-plugin/plugin.yml
