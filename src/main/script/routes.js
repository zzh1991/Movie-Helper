import React from 'react';
import { HashRouter as Router, Route, Switch, Redirect } from 'react-router-dom';
import { Provider } from 'react-redux';
import Loadable from 'react-loadable';
import store from './store';
import NotFound from './containers/notFound';

function Loading() {
  return <div>Loading...</div>;
}

const SideBarContainer = Loadable({
  loader: () => import('./containers/sidebarContainer'),
  loading: Loading,
});

const TopMoviesContainer = Loadable({
  loader: () => import('./containers/topMoviesContainer'),
  loading: Loading,
});

const ViewedMoviesContainer = Loadable({
  loader: () => import('./containers/viewedMoviesContainer'),
  loading: Loading,
});

const StarMoviesContainer = Loadable({
  loader: () => import('./containers/starMoviesContainer'),
  loading: Loading,
});

const Routes = () => (
  <Provider store={store}>
    <Router>
      <div>
        <Switch>
          <Route exact path="/" component={SideBarContainer} />
          <Route path="/top" component={TopMoviesContainer} />
          <Route path="/view" component={ViewedMoviesContainer} />
          <Route path="/star" component={StarMoviesContainer} />
          <Route path='/404' component={NotFound} />
          <Redirect to="/404" />
        </Switch>
      </div>
    </Router>
  </Provider>
);

export default Routes;
