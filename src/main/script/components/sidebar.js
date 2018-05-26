import React from 'react';
import PropTypes from 'prop-types';
import { Layout, Icon, Menu, Button } from 'antd';
import { withRouter } from 'react-router-dom';

const { Header, Content, Sider } = Layout;
class SideBar extends React.Component {
  static defaultProps = {
    showSyncButton: false,
  }

  constructor(props) {
    super(props);
    this.state = {
      collapsed: false,
    };
  }

  toPath = (path) => {
    const { router } = this.context;
    router.history.push(path);
  };

  toggle = () => {
    this.setState({
      collapsed: !this.state.collapsed,
    });
  };

  clickMenu = (e) => {
    this.toPath(e.key);
  };

  render() {
    return (
      <Layout>
        <Header
          className="header"
          style={{
            display: 'grid',
            gridTemplateColumns: '5fr 5fr',
          }}
        >
          <div style={{ fontSize: 20, color: 'white', marginLeft: -30 }}>
            <Icon
              className="trigger"
              type={this.state.collapsed ? 'menu-unfold' : 'menu-fold'}
              onClick={this.toggle}
            />
            {'  电影助手'}
          </div>
          {this.props.showSyncButton &&
          <div
            style={{
              display: 'grid',
              marginRight: -30,
              justifyItems: 'end',
              alignItems: 'center',
            }}
          >
            <Button
              icon="sync"
              ghost
              onClick={this.props.syncMovies}
              className="sync-button"
            >
              SYNC
            </Button>
          </div>
          }
        </Header>
        <Layout style={{ height: '92vh' }} >
          <Sider
            trigger={null}
            collapsible
            collapsed={this.state.collapsed}
          >
            <Menu
              theme="dark"
              mode="inline"
              onClick={this.clickMenu}
              defaultSelectedKeys={[this.props.keys]}
            >
              <Menu.Item key="/">
                <Icon type="home" />
                <span>上映电影</span>
              </Menu.Item>
              <Menu.Item key="/top">
                <Icon type="heart" />
                <span>Top 100</span>
              </Menu.Item>
              <Menu.Item key="/view">
                <Icon type="eye" />
                <span>已观影</span>
              </Menu.Item>
              <Menu.Item key="/star">
                <Icon type="star" />
                <span>想看</span>
              </Menu.Item>
            </Menu>
          </Sider>
          <Content style={{ padding: 20 }} >
            {this.props.children}
          </Content>
        </Layout>
      </Layout>
    );
  }
}

SideBar.contextTypes = {
  router: PropTypes.object,
};

SideBar.propTypes = {
  showSyncButton: PropTypes.bool,
};

export default withRouter(SideBar);
