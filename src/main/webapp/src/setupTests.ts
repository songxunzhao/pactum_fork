import { configure } from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';
import 'jest-localstorage-mock';
import fetch, { GlobalWithFetchMock } from 'jest-fetch-mock';

jest.mock('./api', () => {
  return {
    ...jest.requireActual('./api'),
  };
});

const customGlobal: GlobalWithFetchMock = global as GlobalWithFetchMock;
customGlobal.fetch = fetch as any;
customGlobal.fetchMock = customGlobal.fetch;

configure({ adapter: new Adapter() });

export default undefined;
