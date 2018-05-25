import React from 'react';
import ReactDOM from 'react-dom';
import LogRocket from 'logrocket';
import Routes from './routes';

LogRocket.init('zbiy5p/dev-test');
LogRocket.identify('movie-helper', {
  name: 'movie-helper',
});

ReactDOM.render(
  <Routes />,
  document.getElementById('react'),
);

if (module.hot) module.hot.accept('./routes', () => render(Routes));
