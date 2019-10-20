import React from "react";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom";
import { Changeset, Repository } from "@scm-manager/ui-types";
import {
  fetchChangesetIfNeeded,
  getChangeset,
  getFetchChangesetFailure,
  isFetchChangesetPending
} from "../modules/changesets";
import ChangesetDetails from "../components/changesets/ChangesetDetails";
import { translate } from "react-i18next";
import { ErrorPage, Loading } from "@scm-manager/ui-components";

type Props = {
  id: string;
  changeset: Changeset;
  repository: Repository;
  loading: boolean;
  error: Error;
  fetchChangesetIfNeeded: (repository: Repository, id: string) => void;
  match: any;
  t: (p: string) => string;
};

class ChangesetView extends React.Component<Props> {
  componentDidMount() {
    const { fetchChangesetIfNeeded, repository } = this.props;
    const id = this.props.match.params.id;
    fetchChangesetIfNeeded(repository, id);
  }

  render() {
    const { changeset, loading, error, t, repository } = this.props;

    if (error) {
      return (
        <ErrorPage
          title={t("changesets.errorTitle")}
          subtitle={t("changesets.errorSubtitle")}
          error={error}
        />
      );
    }

    if (!changeset || loading) return <Loading />;

    return <ChangesetDetails changeset={changeset} repository={repository} />;
  }
}

const mapStateToProps = (state, ownProps: Props) => {
  const repository = ownProps.repository;
  const id = ownProps.match.params.id;
  const changeset = getChangeset(state, repository, id);
  const loading = isFetchChangesetPending(state, repository, id);
  const error = getFetchChangesetFailure(state, repository, id);
  return {
    changeset,
    error,
    loading
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchChangesetIfNeeded: (repository: Repository, id: string) => {
      dispatch(fetchChangesetIfNeeded(repository, id));
    }
  };
};

export default withRouter(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(translate("repos")(ChangesetView))
);
