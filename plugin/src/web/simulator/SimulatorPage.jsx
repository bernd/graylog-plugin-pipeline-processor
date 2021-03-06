import React from 'react';
import { Button, Row, Col } from 'react-bootstrap';
import { LinkContainer } from 'react-router-bootstrap';

import { PageHeader, Spinner } from 'components/common';
import DocumentationLink from 'components/support/DocumentationLink';
import ProcessorSimulator from './ProcessorSimulator';

import StoreProvider from 'injection/StoreProvider';
const StreamsStore = StoreProvider.getStore('Streams');

import DocsHelper from 'util/DocsHelper';
import Routes from 'routing/Routes';

const SimulatorPage = React.createClass({
  getInitialState() {
    return {
      streams: undefined,
    };
  },

  componentDidMount() {
    StreamsStore.listStreams().then((streams) => {
      streams.push({
        id: 'default',
        title: 'Default',
        description: 'Stream used by default for messages not matching another stream.',
      });
      this.setState({ streams: streams });
    });
  },

  _isLoading() {
    return !this.state.streams;
  },

  render() {
    let content;
    if (this._isLoading()) {
      content = <Spinner/>;
    } else {
      content = <ProcessorSimulator streams={this.state.streams} />;
    }

    return (
      <div>
        <PageHeader title="Simulate processing" experimental>
          <span>
            Processing messages can be complex. Use this page to simulate the result of processing an incoming{' '}
            message using your current set of pipelines and rules.
          </span>
          <span>
            Read more about Graylog pipelines in the <DocumentationLink page={DocsHelper.PAGES.PIPELINES} text="documentation" />.
          </span>

          <span>
            <LinkContainer to={Routes.pluginRoute('SYSTEM_PIPELINES')}>
              <Button bsStyle="info">Manage pipelines</Button>
            </LinkContainer>
            &nbsp;
            <LinkContainer to={Routes.pluginRoute('SYSTEM_PIPELINES_RULES')}>
              <Button bsStyle="info">Manage rules</Button>
            </LinkContainer>
          </span>
        </PageHeader>

        <Row className="content">
          <Col md={12}>
            {content}
          </Col>
        </Row>
      </div>
    );
  },
});

export default SimulatorPage;
